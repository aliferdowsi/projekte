#include <ros/ros.h>
#include <sensor_msgs/Image.h>
#include <sensor_msgs/LaserScan.h>
#include <opencv2/opencv.hpp>
#include <cv_bridge/cv_bridge.h>
#include <visualization_msgs/Marker.h>
#include <ros/ros.h>
#include <geometry_msgs/TransformStamped.h>
#include <geometry_msgs/PointStamped.h>
#include <geometry_msgs/QuaternionStamped.h>
#include <nav_msgs/OccupancyGrid.h>
#include <tf2_ros/transform_listener.h>
#include <tf2_ros/transform_broadcaster.h>
#include <tf2/utils.h>
#include <geometry_msgs/TransformStamped.h>
#include <cmath>
#include <message_filters/subscriber.h>
#include <message_filters/synchronizer.h>
#include <message_filters/sync_policies/approximate_time.h>
#include <message_filters/sync_policies/exact_time.h>
ros::Publisher pub;
ros::Publisher pub_red_triangle;
ros::Publisher pub_blue_square;
using namespace cv;
using namespace std;
using namespace message_filters;

double dist_90;
double pos_robot_x;
double pos_robot_y;
double pos_robot_z;
double winkel_object;
int winkel_laserscan;
int winkel;
bool object_found = false;
Mat img_test;
vector<Point> found_squares;
vector<Point> found_triangles;
vector<Point> square_queue;
vector<Point> triangle_queue;

geometry_msgs::TransformStamped transformStamped;
sensor_msgs::LaserScan scan;
static double angle( Point pt1, Point pt2, Point pt0 )
{
    double dx1 = pt1.x - pt0.x;
    double dy1 = pt1.y - pt0.y;
    double dx2 = pt2.x - pt0.x;
    double dy2 = pt2.y - pt0.y;

    return (dx1*dx2 + dy1*dy2)/sqrt((dx1*dx1 + dy1*dy1)*(dx2*dx2 + dy2*dy2) + 1e-10);
}

void callbackmap(nav_msgs::OccupancyGrid msg){

}

// gets average of a vector of Points for both x and y. The result first index is x and second index represent y
vector<double> getVectorPointAverage(vector<Point> vec) {
    double avg_x = 0;
    double avg_y = 0;
    vector<double> result;
    for ( Point p : vec){
        avg_x += p.x;
        avg_y += p.y; 
    }
    avg_x = avg_x / 4;
    avg_y = avg_y / 4;

    result.push_back(avg_x);
    result.push_back(avg_y);
}

bool check_queue(vector<Point> queue){
    vector<double> avgVec = getVectorPointAverage(queue);

    for(Point p : squeue){
        double temp_x = abs(p.x - avgVec.get(0));
        double temp_y = abs(p.y - avgVec.get(1));
        if( temp_x > 0.6 || temp_y > 0.6) {
            return false;
        }
    }
    return true;
}


bool check_mehrfach_triangle(Point point){
    for (Point p : found_triangles){
        if(point.x <= p.x + 0.6 && point.x >= p.x -0.6 && 
            p.y <= p.y + 0.6 && point.y >= p.y -0.6   )
            return false;
    }
    return true;
}




bool check_mehrfach_square(Point point){
    for (Point p : found_squares){
        if(point.x <= p.x + 0.6 && point.x >= p.x -0.6 && 
            p.y <= p.y + 0.6 && point.y >= p.y -0.6   )
            return false;
    }
    return true;
}

double find_middle_triangle(std::vector<Point> points){
    if(points.size() == 0 ){cout << "ERROR-> TRINAGLE 0 CONTOUR"; return 0.0;};
    double max_width = points.at(0).x;
    double low_width = points.at(0).y;
    for(Point p : points){
        if(p.x > max_width) max_width = p.x;
        if(p.x < low_width) low_width = p.x;
    }
    double halb_len = (max_width - low_width)/2;
    return low_width + halb_len;
}

void callbacklaser(sensor_msgs::LaserScan scanlaser)
{   
    scan = scanlaser;
}

void callback(const sensor_msgs::ImageConstPtr& img, const sensor_msgs::LaserScanPtr& scan)
{   
    cv_bridge::CvImagePtr cv_img_ptr;
    try
    { // Making a Copy of the oiginal IMG into a CV IMAGE Type
        cv_img_ptr = cv_bridge::toCvCopy(img, sensor_msgs::image_encodings::BGR8);
    }
    catch(cv_bridge::Exception& e)
    {
        ROS_ERROR("cv_bridge exception: %s", e.what());
        return;
    }

    // Mat Does memoy allocation it self
    Mat blurred_image;
    // Gaussianblu based on intensity so it helps to give more difiniton to edges
    GaussianBlur(cv_img_ptr->image, blurred_image, Size(5,5), 0);
    // CV2 to HSV
    Mat hsv;
    cvtColor(blurred_image,hsv,CV_BGR2HSV); //to HSV muss
    //cvtColor(blurred_image,hsv,COLOR_BGR2HSV); //to HSV muss
    //cout << "auflösung" << hsv.rows << ", " << hsv.cols << endl; 
    // BLUE SQUARE
    Mat threshhold_blue;

    inRange(hsv,Scalar(85,50,40), Scalar(135,255,255),threshhold_blue);
    //imshow("thresh_blue",threshhold_blue);
    //Find contours
    std::vector<std::vector<Point> > contours;
    findContours( threshhold_blue, contours ,RETR_CCOMP , CHAIN_APPROX_NONE );
    int count = 0;
    std::vector<Point> approx;
    std::vector<std::vector<Point>> result;

    for (size_t i = 0; i < contours.size(); i++){
        approxPolyDP(Mat(contours[i]), approx, arcLength(Mat(contours[i]), true)*0.02, true);
        if (approx.size() == 4 && fabs(contourArea(Mat(approx))) > 300 && isContourConvex(Mat(approx))){
            
            double maxCosine = 0;
            // j = 2   --> angle(approx[2%4], approx[0], approx[1] 
            // j = 3   --> angle(approx[3%4], approx[1], approx[2]
            // j = 4   --> angle(approx[4%4], approx[2], approx[3]
            for( int j = 2; j < 5; j++ ){
                double cosine = fabs(angle(approx[j%4], approx[j-2], approx[j-1]));
            
                maxCosine = MAX(maxCosine, cosine);
            }
            if( maxCosine < 0.2 ){ // 0 = 90 degree
                Rect rect = boundingRect(approx);

                // Begin of new Code:

                double middle_x = rect.x + rect.width; // Middle of Square in Picture in Pixel

                double ratio = -1*(( middle_x - 320.0 ) / 320.0);


                // range of angle: -30 <-> 30
                winkel = ratio * 30; // now you have the angle (estimate) 30 degree = to the left, -30 degree to the right
                if(scan->ranges.size() == 0 )return;
                
                if (winkel < 0) winkel = 360 + winkel;

                tf2::Quaternion q;
                tf2::convert(transformStamped.transform.rotation, q);
                double rotation = tf2::getYaw(q);
                double pos_marker_x;
                double pos_marker_y;
               
                pos_marker_x = pos_robot_x + scan->ranges.at(winkel) * cos(winkel * scan->angle_increment + scan->angle_min + rotation);
                pos_marker_y = pos_robot_y + scan->ranges.at(winkel) * sin(winkel * scan->angle_increment + scan->angle_min + rotation);
                Point point(pos_marker_x,pos_marker_y);
                
                square_queue.push_back(point);

                if(square_queue.size() == 4){
                    if ( check_queue(square_queue) == true ) {
                        vector<Point> queue_avg = getVectorPointAverage(square_queue);
                        Point queue_avg_point(queue_avg.get(0), queue_avg.get(1));
                        if(check_mehrfach_square(getVectorPointAverage(queue_avg)) == true){
                            cout << "SQUARE FOUND->"<< " x = " << queue_avg.get(0) <<" ,y= "<< queue_avg.get(1) <<endl;   
                            found_squares.push_back(queue_avg.push_back(Point()));
                            visualization_msgs::Marker marker;
                            marker.pose.position.x = pos_marker_x;
                            marker.pose.position.y = pos_marker_y;
                            marker.pose.position.z = 0;
                            pub_red_triangle.publish(marker);
                        }
                    }
                    square_queue.clear();
                }
	            
            }   
        }
    }
    count = 0;
    // TRIANGLE
    cvtColor(blurred_image,hsv,COLOR_BGR2HSV); 

    Mat threshhold_red;

    inRange(hsv,Scalar(0,100,20), Scalar(5,255,255),threshhold_red);

    Mat mask_red = threshhold_red;

    //Find contours
    std::vector<std::vector<Point> > contours_triangle;
    findContours( mask_red, contours_triangle ,RETR_CCOMP , CHAIN_APPROX_NONE );
    std::vector<Point> approx_triangle;

    
    for (size_t i = 0; i < contours_triangle.size(); i++){
        
        approxPolyDP(Mat(contours_triangle[i]), approx_triangle, arcLength(Mat(contours_triangle[i]), true)*0.02, true);
        
        if (approx_triangle.size() == 3 && fabs(contourArea(Mat(approx_triangle))) > 500 && isContourConvex(Mat(approx_triangle))){
            double middle_x = find_middle_triangle(approx_triangle);
            double ratio = -1*(( middle_x - 320.0 ) / 320.0);
            winkel = ratio * 30; // now you have the angle (estimate) 30 degree = to the left, -30 degree to the right
            if(scan->ranges.size() == 0 )return;
            
            if (winkel < 0) winkel = 360 + winkel;

            tf2::Quaternion q;
            tf2::convert(transformStamped.transform.rotation, q);
            double rotation = tf2::getYaw(q);
            double pos_marker_x;
            double pos_marker_y;
            pos_marker_x = pos_robot_x + scan->ranges.at(winkel) * cos(winkel * scan->angle_increment + scan->angle_min + rotation);
            pos_marker_y = pos_robot_y + scan->ranges.at(winkel) * sin(winkel * scan->angle_increment + scan->angle_min + rotation);

            Point point(pos_marker_x,pos_marker_y);
                
            square_queue.push_back(point);

            if(square_queue.size() == 4){
                if ( check_queue(triangle_queue) == true ) {
                    vector<Point> queue_avg = getVectorPointAverage(triangle_queue);
                    Point queue_avg_point(queue_avg.get(0), queue_avg.get(1));
                    if(check_mehrfach_square(getVectorPointAverage(queue_avg)) == true){
                        cout << "SQUARE FOUND->"<< " x = " << queue_avg.get(0) <<" ,y= "<< queue_avg.get(1) <<endl;   
                        found_triangles.push_back(queue_avg_point);
                        visualization_msgs::Marker marker;
                        marker.pose.position.x = pos_marker_x;
                        marker.pose.position.y = pos_marker_y;
                        marker.pose.position.z = 0;
                        pub_red_triangle.publish(marker);
                    }
                }
                square_queue.clear();
            }

            /*
            tuple<double,double>obj(pos_marker_x,pos_marker_y);

            if(check_mehrfach_triangle(obj) == true){
                cout << "TRIANGLE FOUND->"<< " x = " <<pos_marker_x <<" ,y= "<<pos_marker_y<<endl;   
                found_triangles.push_back(obj);
                visualization_msgs::Marker marker;
                marker.pose.position.x = pos_marker_x;
                marker.pose.position.y = pos_marker_y;
                marker.pose.position.z = 0;
                pub_red_triangle.publish(marker);
            }
	        */    

        }
    }
     
    //Find Edges

    //bitwise_and
    
    pub.publish(cv_bridge::CvImage(cv_img_ptr->header, sensor_msgs::image_encodings::BGR8, blurred_image).toImageMsg());
}

int main(int argc, char **argv)
{
    ros::init(argc, argv, "opencv_test_node");
    ros::NodeHandle nh;
    ros::Subscriber map = nh.subscribe("/map",1000,callbackmap);
    pub = nh.advertise<sensor_msgs::Image>("/blurred_image", 10);
    pub_blue_square = nh.advertise<visualization_msgs::Marker>("/blue_square_pos",5);
    pub_red_triangle = nh.advertise<visualization_msgs::Marker>("/red_triangle_pos",5);
    message_filters::Subscriber<sensor_msgs::Image> image_sub(nh, "/camera/image", 1);
    message_filters::Subscriber<sensor_msgs::LaserScan> laser_sub(nh, "/scan", 1);
    /*
    typedef sync_policies::ApproximateTime<sensor_msgs::Image,sensor_msgs::LaserScan> MySyncPolicy;
    Synchronizer<MySyncPolicy> sync(MySyncPolicy(10), image_sub, laser_sub);
    sync.registerCallback(boost::bind(&callback, _1, _2));
    */
    // get current location of robot
    tf2_ros::Buffer tfBuffer;
    tf2_ros::TransformListener tfListener(tfBuffer);
    ros::Rate rate(10.0);
    while (ros::ok())
    {
        try{
            transformStamped = tfBuffer.lookupTransform("map", "base_footprint", ros::Time::now());
        }
        catch (tf2::TransformException &ex) {continue;} 
        pos_robot_x = transformStamped.transform.translation.x;
        pos_robot_y = transformStamped.transform.translation.y;
        pos_robot_z = transformStamped.transform.translation.z;
        ros::spinOnce();
        
    }
    
    // transform world point into local point with transformation here
    ros::spin();

}


