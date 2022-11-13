#include "mainwindow.h"
#include "./ui_mainwindow.h"
#include "stdlib.h"
#include <QTextStream>
#include "thread_gameplay.h"
#include <tuple>

QTextStream cout(stdout);
QString input_player_name;
int input_player_chip;
int input_small_blind;
int input_big_blind;
int count_sits = 0;
QString player1_name;
QString player2_name;
QString player3_name;
QString player4_name;
QString player5_name;
QString player6_name;
QString player7_name;
QString player8_name;
QString player9_name;
std::vector<QString> player_names;
GAMEPLAY gameplay;
DECK d;
bool wait_result_screen = false;
QString PATH_TO_DIR  = QDir::currentPath() + "/";




MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
    , ui(new Ui::MainWindow)
{

    ui->setupUi(this);
    qDebug() <<  QDir::currentPath();
    QPixmap pic = QPixmap(PATH_TO_DIR + "/images/a.png"); // starting Image
    ui->img_start->setPixmap(pic);

    QPixmap pic2 = QPixmap(PATH_TO_DIR + "/images/b.jpg"); // Player Select Image
    ui->img_player_select->setPixmap(pic2);

    QPixmap pic3 = QPixmap(PATH_TO_DIR + "/images/c.jpg"); // Table Image
    ui->img_table->setPixmap(pic3);

    QPixmap pic4 = QPixmap(PATH_TO_DIR + "/images/c.jpg"); // Table Image
    ui->img_table2->setPixmap(pic4);

    // Hiding Errors for Page 2



    //Hiding Player names after text panel for player names;
    ui->show_p_1->hide();
    ui->show_p_2->hide();
    ui->show_p_3->hide();
    ui->show_p_4->hide();
    ui->show_p_5->hide();
    ui->show_p_6->hide();
    ui->show_p_7->hide();
    ui->show_p_8->hide();
    ui->show_p_9->hide();

    //Hiding Player names for game table page
    QVector<QLabel*> QLabel_Players = {ui->show_player_1,ui->show_player_2,ui->show_player_3,ui->show_player_4,ui->show_player_5,ui->show_player_6,ui->show_player_7,
                                                   ui->show_player_8,ui->show_player_9};

    for(QLabel* text : QLabel_Players){
        text->hide();
        text->setAlignment(Qt::AlignCenter);
    }
    // Hiding player chips
    ui->text_chip_p_1->hide();
    ui->text_chip_p_2->hide();
    ui->text_chip_p_3->hide();
    ui->text_chip_p_4->hide();
    ui->text_chip_p_5->hide();
    ui->text_chip_p_6->hide();
    ui->text_chip_p_7->hide();
    ui->text_chip_p_8->hide();
    ui->text_chip_p_9->hide();


}

MainWindow::~MainWindow()
{
    delete ui;
}




void MainWindow::on_btn_play_clicked()
{
    ui->stackedWidget->setCurrentIndex(1);

}


void MainWindow::on_btn_player_ok_clicked()
{

    input_player_name = ui->input_5->toPlainText();
    if ( input_player_name != ""  ){
       on_testbutton_clicked();
    }
}


std::vector<QString> get_player_names(){
    return player_names;
}

void MainWindow::set_big_or_small_blind(){
    QLabel* is_s_or_b_x;

    for(int i=0; i<gameplay.players.size(); i++){
        is_s_or_b_x = this->findChild<QLabel*>("is_s_or_b_" + QString::number(gameplay.players.at(i).table_sit));
        if(gameplay.players.at(i).is_SMALL_BLIND){
            is_s_or_b_x->setText("S");
        } else if (gameplay.players.at(i).is_BIG_BLIND){
            is_s_or_b_x->setText("B");
        } else {
            is_s_or_b_x->setText("");
        }
    }
}

void MainWindow::set_evaluation_for_each_player(){
    QLabel* eval_p_x;

    for(int i = 0; i<gameplay.players.size(); i++){
        PLAYER& temp = gameplay.players.at(i);
        eval_p_x = this->findChild<QLabel*>("eval_p_" + QString::number(temp.table_sit));
        gameplay.evaluation(temp);
        eval_p_x->setText(QString::fromStdString(gameplay.ranks.at(temp.eval-1)) + ": " + QString::number(temp.eval_rank));
    }
}

void MainWindow::evaluation_for_player(){
    QLabel* eval_p_x;
    for(int i = 0; i<gameplay.players.size(); i++){
        if (QString::fromStdString(gameplay.players.at(i).player_name) == input_player_name){
            PLAYER temp = gameplay.players.at(i);
            eval_p_x = this->findChild<QLabel*>("eval_p_" + QString::number(temp.table_sit));
            gameplay.evaluation(temp);
            eval_p_x->setText(QString::fromStdString(gameplay.ranks.at(temp.eval-1)) + ": " + QString::number(temp.eval_rank));
        } else {
            PLAYER temp = gameplay.players.at(i);
            eval_p_x = this->findChild<QLabel*>("eval_p_" + QString::number(temp.table_sit));
            gameplay.evaluation(temp);
            eval_p_x->setText("");
        }
    }
}



void MainWindow::on_btn_start_game_clicked(){ // START OF THE GAME.
    if( count_sits < 2) return;
    gameplay.gameplay_deck = d;
    d.make_deck();

    ui->stackedWidget->setCurrentIndex(3);
    // Check if the Plain texts are not empty meaning players have joined so we save the names in vector<QString>player_names so we can start the game with the names
    // and then we also set the text for the table Widget page and finally show it as well since its hidden from start.
    // we also add players sits based on the sit that starts with 1 to a vector so later on we can use it to make a connection between gameplay.players and the labels and etc of that player position.
    std::vector<PLAYER> players;
    for(int i=1; i<10; i++){
        QTextBrowser* show_p_x = this->findChild<QTextBrowser*>("show_p_" + QString::number(i));
        if(show_p_x->toPlainText() != ""){
            PLAYER temp(show_p_x->toPlainText().toStdString(),100);
            temp.table_sit = i;
            show_p_x->setText(show_p_x->toPlainText());
            show_p_x->show();
            show_p_x->show();
        }
    }
    gameplay.players = players;
    gameplay.start(gameplay.players,d.deck);
    gameplay.setTurn();
    set_big_or_small_blind();
    int count = 0;
    set_start();
    ui->text_turn->setText(QString::fromStdString(gameplay.players.at(gameplay.turn).player_name));
    ui->text_pot->setText(QString::number(gameplay.pot));
    evaluation_for_player();
}

void MainWindow::set_end(){
    for(int i = 0; i < gameplay.players.size();i++){

        PLAYER& player = gameplay.players.at(i);
        QLabel* text_chip_p_ = this->findChild<QLabel*>("text_chip_p_" +  QString::number(player.table_sit));
        QLabel* px_card1 = this->findChild<QLabel*>("p" + QString::number(player.table_sit) + "_card_1" );
        QLabel* px_card2 = this->findChild<QLabel*>("p" + QString::number(player.table_sit) + "_card_2" );
        int num = std::get<0>(player.player_hand.at(0));
        std::string type = std::get<1>(player.player_hand.at(0));
        QString path = PATH_TO_DIR + "/images/cards/" +  QString::fromStdString(std::to_string(num)) + "_of_" + QString::fromStdString(type)+".png";
        QPixmap pic = QPixmap(path);

        px_card1->setPixmap(pic);
        px_card1->show();

        num = std::get<0>(player.player_hand.at(1));
        type = std::get<1>(player.player_hand.at(1));
        path = PATH_TO_DIR + "/images/cards/" +  QString::fromStdString(std::to_string(num)) + "_of_" + QString::fromStdString(type) +".png";
        pic = QPixmap(path);

        px_card2->setPixmap(pic);
        px_card2->show();


        text_chip_p_->setText(QString::number(player.chips));
        text_chip_p_->show();

    }
}

void MainWindow::set_start(){


    for(int i = 0; i < gameplay.players.size();i++){

        PLAYER& player = gameplay.players.at(i);

        if (QString::fromStdString(player.player_name) == input_player_name){
            QLabel* text_chip_p_ = this->findChild<QLabel*>("text_chip_p_" +  QString::number(player.table_sit));

            QLabel* px_card1 = this->findChild<QLabel*>("p" + QString::number(player.table_sit) + "_card_1" );
            QLabel* px_card2 = this->findChild<QLabel*>("p" + QString::number(player.table_sit) + "_card_2" );

            int num = std::get<0>(player.player_hand.at(0));
            std::string type = std::get<1>(player.player_hand.at(0));
            QString path = PATH_TO_DIR + "/images/cards/" +  QString::fromStdString(std::to_string(num)) + "_of_" + QString::fromStdString(type)+".png";
            QPixmap pic = QPixmap(path);
            px_card1->setPixmap(pic);
            px_card1->show();

            num = std::get<0>(player.player_hand.at(1));
            type = std::get<1>(player.player_hand.at(1));
            path = PATH_TO_DIR + "/images/cards/" +  QString::fromStdString(std::to_string(num)) + "_of_" + QString::fromStdString(type) +".png";
            pic = QPixmap(path);

            px_card2->setPixmap(pic);
            px_card2->show();


            text_chip_p_->setText(QString::number(player.chips));
            text_chip_p_->show();
        } else {
            QLabel* text_chip_p_ = this->findChild<QLabel*>("text_chip_p_" +  QString::number(player.table_sit));

            QLabel* px_card1 = this->findChild<QLabel*>("p" + QString::number(player.table_sit) + "_card_1" );
            QLabel* px_card2 = this->findChild<QLabel*>("p" + QString::number(player.table_sit) + "_card_2" );

            QString path = PATH_TO_DIR + "/images/cards/back.png";
            QPixmap pic = QPixmap(path);
            px_card1->setPixmap(pic);
            px_card1->show();

            path = PATH_TO_DIR + "/images/cards/back.png";
            pic = QPixmap(path);

            px_card2->setPixmap(pic);
            px_card2->show();


            text_chip_p_->setText(QString::number(player.chips));
            text_chip_p_->show();
        }

    }
}

void MainWindow::set_fold(QString& name){
    QLabel* label = this->findChild<QLabel*>(name);
    if(label){
        label->hide();
    }
}

void MainWindow::set_chip(QString& name){
    QTextBrowser* label = this->findChild<QTextBrowser*>(name);
    if(label){
        label->hide();
    }
}
void MainWindow::on_btn_fold_clicked()
{
    if (wait_result_screen || QString::fromStdString(gameplay.players.at(gameplay.turn).player_name) != input_player_name)return;

    PLAYER player = gameplay.players.at(gameplay.turn);
    gameplay.fold();
    check_round_end();
    std::string temp = "p"+std::to_string(player.table_sit)+"_card_1";
    QString name = QString::fromStdString(temp);
    set_fold(name);
    temp = "p"+std::to_string(player.table_sit)+"_card_2";
    name = QString::fromStdString(temp);
    set_fold(name);
    update_turn();
    check_round_end();
    AI_USE_TURN();
}

void MainWindow::fold_AI()
{
    if (wait_result_screen)return;

    PLAYER player = gameplay.players.at(gameplay.turn);
    gameplay.fold();
    qDebug() << "here";
    check_round_end();
    std::string temp = "p"+std::to_string(player.table_sit)+"_card_1";
    QString name = QString::fromStdString(temp);
    set_fold(name);
    temp = "p"+std::to_string(player.table_sit)+"_card_2";
    name = QString::fromStdString(temp);
    set_fold(name);
    update_turn();
    check_round_end();
    if (gameplay.players.size() > 1 )AI_USE_TURN();
    AI_USE_TURN();
}
void MainWindow::on_btn_sit_6_toggled(bool checked){}

void MainWindow::update_turn(){
    if(gameplay.players.size() == 1)return;
    ui->text_turn->setText(QString::fromStdString(gameplay.players.at(gameplay.turn).player_name));
}

void MainWindow::check_round_end(){
    if (gameplay.check_all_player_all_in(d.deck)){ // IF ALL THE PLAYER HAVE ALL IN.
        switch (gameplay.stage){
            case preflop:
                gameplay.stage = flop;
                gameplay.begin_preflop(d.deck);
                begin_preflop_gui();
                update_turn();
            case flop:
                gameplay.stage = turn;
                gameplay.begin_flop(d.deck);
                begin_flop_gui();
                update_turn();
            case turn:
                gameplay.stage = river;
                gameplay.begin_flop(d.deck);
                begin_turn_gui();
                update_turn();
            case river:
                d.deck.clear();
                d.make_deck();
                set_evaluation_for_each_player();
                set_end();
                gameplay.round_finished(d.deck);
                begin_river_gui();
                update_turn();
                break;
        }
        return;
    }
    if(gameplay.started == 1 && gameplay.first_player == gameplay.turn){  // IF THE ROUND HAS ENDED WITH THE TURN BEING EQUAL TO THE FIRST PLAYER THAT STARTED THE ROUND/RAISED
        switch (gameplay.stage){
            case preflop:
                gameplay.stage = flop;
                gameplay.begin_preflop(d.deck);
                begin_preflop_gui();
                update_turn();
                break;
            case flop:
                gameplay.stage = turn;
                gameplay.begin_flop(d.deck);
                begin_flop_gui();
                update_turn();
                break;
            case turn:
                gameplay.stage = river;
                gameplay.begin_flop(d.deck);
                begin_turn_gui();
                update_turn();
                break;
            case river:
                d.deck.clear();
                d.make_deck();
                set_evaluation_for_each_player();
                set_end();
                gameplay.round_finished(d.deck);
                begin_river_gui();
                update_turn();
                break;
        }
    }

}

void MainWindow::begin_preflop_gui(){
    qDebug() << "Flop Started";
    ui->text_raise->hide();
    QString table_card_1 = PATH_TO_DIR + "/images/cards/" + QString::number(get<0>(gameplay.TABLE.at(0))) + "_of_" + QString::fromStdString(get<1>(gameplay.TABLE.at(0))) + ".png";
    QString table_card_2 = PATH_TO_DIR + "/images/cards/" +QString::number(get<0>(gameplay.TABLE.at(1))) + "_of_" + QString::fromStdString(get<1>(gameplay.TABLE.at(1))) + ".png";
    QString table_card_3 = PATH_TO_DIR + "/images/cards/" +QString::number(get<0>(gameplay.TABLE.at(2))) + "_of_" + QString::fromStdString(get<1>(gameplay.TABLE.at(2))) + ".png";
    ui->table_card_1->setPixmap(QPixmap(table_card_1));
    ui->table_card_2->setPixmap(QPixmap(table_card_2));
    ui->table_card_3->setPixmap(QPixmap(table_card_3));
    evaluation_for_player();
}

void MainWindow::begin_flop_gui(){
    qDebug() << "turn Started";
    ui->text_raise->hide();
    QString table_card_4 = PATH_TO_DIR + "/images/cards/" +QString::number(get<0>(gameplay.TABLE.at(3))) + "_of_" + QString::fromStdString(get<1>(gameplay.TABLE.at(3))) + ".png";
    ui->table_card_4->setPixmap(QPixmap(table_card_4));
    evaluation_for_player();

}

void MainWindow::begin_turn_gui(){
    qDebug() << "river Started";
    ui->text_raise->hide();
    QString table_card_5 = PATH_TO_DIR + "/images/cards/" +QString::number(get<0>(gameplay.TABLE.at(4))) + "_of_" + QString::fromStdString(get<1>(gameplay.TABLE.at(4))) + ".png";
    ui->table_card_5->setPixmap(QPixmap(table_card_5));
    evaluation_for_player();
};

void MainWindow::lost_players_remove(){
    for(int i=0; i<gameplay.lost_players.size() ;i++){
        int table_turn = gameplay.lost_players.at(i).table_sit;
        QLabel* text_chip_p_ = this->findChild<QLabel*>("text_chip_p_" +  QString::number(table_turn));
        QLabel* px_card1 = this->findChild<QLabel*>("p" + QString::number(table_turn) + "_card_1" );
        QLabel* px_card2 = this->findChild<QLabel*>("p" + QString::number(table_turn) + "_card_2" );
        QTextBrowser* show_p_x = this->findChild<QTextBrowser*>("show_p_" + QString::number(table_turn));
        QLabel* show_player_x = this->findChild<QLabel*>("show_player_" + QString::number(table_turn));
        QLabel* eval_p_x = this->findChild<QLabel*>("eval_p_" + QString::number(table_turn));
        QLabel* is_s_or_b_x = this->findChild<QLabel*>("is_s_or_b_" + QString::number(table_turn));

        eval_p_x->hide();
        show_player_x->hide();
        show_p_x->setPlainText("");
        text_chip_p_->hide();
        px_card1->hide();
        px_card2->hide();
        is_s_or_b_x->hide();
    }
}

void MainWindow::table_clean_cards(){
    for(int i = 1; i<6; i++){
        QLabel* table_card_x = this->findChild<QLabel*>("table_card_" + QString::number(i));
        table_card_x->setPixmap(QPixmap(""));

    }

}


void MainWindow::begin_river_gui(){
    QEventLoop loop;
    wait_result_screen = true;
    ui->text_turn_or_winner->setText("WINNER:");
    ui->text_turn->setText(QString::fromStdString(gameplay.last_winner.player_name));


    for(int i=0; i< gameplay.players.size();i++){
        QString temp = "text_chip_p_" + QString::number(gameplay.players.at(i).table_sit);
        QLabel* test_chip_p_x = this->findChild<QLabel*>(temp);
        test_chip_p_x->setText(QString::number(gameplay.players.at(i).chips));
    }
    ui->text_pot->setText(QString::number(gameplay.pot));

    QTimer::singleShot(12000,&loop,&QEventLoop::quit);
    loop.exec();
    gameplay.setTurn();
    ui->text_turn_or_winner->setText("TURN");
    ui->text_turn->setText(QString::fromStdString((gameplay.players.at(gameplay.turn).player_name)));
    // Here we set all the Labels for all the image cards anew.
    lost_players_remove();
    set_start();
    table_clean_cards();
    ui->text_raise->hide();
    wait_result_screen = false;
    evaluation_for_player();
    set_big_or_small_blind();
};


bool MainWindow::on_btn_call_clicked()
{
    //text_chip_p_1
    if (wait_result_screen || QString::fromStdString(gameplay.players.at(gameplay.turn).player_name) != input_player_name)return false;

    int turn = gameplay.turn;
    if (gameplay.call() == true ){
        QString text_chip_p_x = "text_chip_p_" + QString::number(gameplay.players.at(turn).table_sit);
        QLabel* text_chip_p_x_ = this->findChild<QLabel*>(text_chip_p_x);
        if(text_chip_p_x_){
            text_chip_p_x_->setText(QString::number(gameplay.players.at(turn).chips));
            ui->text_pot->setText(QString::number(gameplay.pot));
        }
        update_turn();
        check_round_end();
        gameplay.started = 1;
        AI_USE_TURN();
        return true;
    }
    return false;
}

bool MainWindow::call_AI()
{
    //text_chip_p_1
    if (wait_result_screen)return false;

    int turn = gameplay.turn;
    if (gameplay.call()){
        QString text_chip_p_x = "text_chip_p_" + QString::number(gameplay.players.at(turn).table_sit);
        QLabel* text_chip_p_x_ = this->findChild<QLabel*>(text_chip_p_x);
        if(text_chip_p_x_){
            text_chip_p_x_->setText(QString::number(gameplay.players.at(turn).chips));
            ui->text_pot->setText(QString::number(gameplay.pot));
        }
        update_turn();
        check_round_end();
        gameplay.started = 1;
        AI_USE_TURN();
        return true;
    }
    return false;
}

bool MainWindow::AI_raise(int to_raise)
{
    if (wait_result_screen)return false;
    int raise_turn = gameplay.turn;
    if (gameplay.raise(to_raise)){
        //text_chip_p_9
        QString text_chip_p_x = "text_chip_p_" + QString::number(gameplay.players.at(raise_turn).table_sit);
        QLabel* text_chip_p_x_ = this->findChild<QLabel*>(text_chip_p_x);
        if(text_chip_p_x_){
            text_chip_p_x_->setText(QString::number(gameplay.players.at(raise_turn).chips));
            ui->text_pot->setText(QString::number(gameplay.pot));
        }
        update_turn();
        ui->text_raise->setText(QString::fromStdString(gameplay.raised_player.player_name) + " has raised" + QString::number(gameplay.raised_amount));
        ui->text_raise->show();
        check_round_end();
        gameplay.started = 1;
        AI_USE_TURN();
        return true;
    }
    return false;
}


bool MainWindow::on_btn_raise_clicked()
{
    if (wait_result_screen || QString::fromStdString(gameplay.players.at(gameplay.turn).player_name) != input_player_name)return false;

    bool valid;
    int to_raise = ui->input_raise->toPlainText().toInt(&valid);
    int raise_turn = gameplay.turn;
    if (ui->input_raise->toPlainText() != "" && valid && gameplay.raise(to_raise)){
        //text_chip_p_9
        QString text_chip_p_x = "text_chip_p_" + QString::number(gameplay.players.at(raise_turn).table_sit);
        QLabel* text_chip_p_x_ = this->findChild<QLabel*>(text_chip_p_x);
        if(text_chip_p_x_){
            text_chip_p_x_->setText(QString::number(gameplay.players.at(raise_turn).chips));
            ui->text_pot->setText(QString::number(gameplay.pot));
        }
        update_turn();
        ui->text_raise->setText(QString::fromStdString(gameplay.raised_player.player_name) + " has raised" + QString::number(gameplay.raised_amount));
        ui->text_raise->show();
        check_round_end();
        gameplay.started = 1;
        AI_USE_TURN();
        return true;
    }
    return false;
}





bool MainWindow::on_btn_check_clicked()
{
    if (wait_result_screen || QString::fromStdString(gameplay.players.at(gameplay.turn).player_name) != input_player_name)return false;

    if(gameplay.can_check()){
        gameplay.turn_next();
        update_turn();
        check_round_end();
        gameplay.started = 1;
        AI_USE_TURN();
        return true;
    }
    return false;
}

bool MainWindow::check_AI()
{
    if (wait_result_screen )return false;

    if(gameplay.can_check()){
        gameplay.turn_next();
        update_turn();
        check_round_end();
        gameplay.started = 1;
        AI_USE_TURN();
        return true;
    }
    return false;
}

void MainWindow::AI_USE_TURN(){
    if(gameplay.players.at(gameplay.turn).AI == false)return;

    QEventLoop loop;
    PLAYER& player = gameplay.players.at(gameplay.turn);
    QTimer::singleShot(1000,&loop,&QEventLoop::quit); //  wait 3 sec
    loop.exec();

    std:vector<int> function_vector;
    function_vector.push_back(0); // check
    function_vector.push_back(1); // fold
    function_vector.push_back(2); // call
    function_vector.push_back(3); // raise

    random_device rand;
    mt19937 gen(rand());
    uniform_int_distribution<int> distr(0,3);

    if (player.raised == true || player.small_blind_paid == false ){ // if player is raised
        int random_function = uniform_int_distribution<int>(1,2)(gen);

        switch(random_function){
            case 1: // fold
                if(gameplay.players.size() == 2){
                    if(call_AI() == false){ // if player cant call -> must check or fold.
                        if (check_AI() == false){
                            fold_AI();
                        }
                    }
                } else fold_AI();
            break;
            case 2: // call
                if(call_AI() == false){ // if player cant call -> must check or fold.
                    fold_AI();
                }
            break;
        }
    } else { // if player is not raised
        int random_function = uniform_int_distribution<int>(0,3)(gen);
        int random_raise = uniform_int_distribution<int>(player.chips/2,player.chips)(gen);
        switch(random_function){
            case 0: // check
                if (check_AI() == false){
                    fold_AI();
                }
            break;
            case 1: // call
                if(call_AI() == false){ // if player cant call -> must check or fold.
                    if (check_AI() == false){
                        fold_AI();
                    }
                }
            break;
            case 2: // fold
                on_btn_fold_clicked();
            break;

            case 3: // raise
                qDebug() << "RAISE CASE";
                if (AI_raise(random_raise) == false){
                    if(call_AI() == false){ // if player cant call -> must check or fold.
                        if (check_AI() == false){
                            fold_AI();
                        }
                    }
                }
            break;
        }
    }
    AI_USE_TURN();

}
void MainWindow::on_testbutton_clicked()
{
    gameplay.gameplay_deck = d;
    d.make_deck();

    ui->stackedWidget->setCurrentIndex(3);

    ui->show_p_1->setPlainText("Ali(BOT)");
    ui->show_p_2->setPlainText("Emad(BOT)");
    ui->show_p_3->setPlainText("Mosi(BOT)");
    ui->show_p_4->setPlainText("Saeed(BOT)");
    ui->show_p_5->setPlainText("Ramin(BOT)");
    ui->show_p_6->setPlainText("Aref(BOT)");
    ui->show_p_7->setPlainText("Nima(BOT)");
    ui->show_p_8->setPlainText(input_player_name);
    ui->show_p_9->setPlainText("Poori(BOT)");

    for(int i=1; i<10; i++){
        QTextBrowser* show_p_x = this->findChild<QTextBrowser*>("show_p_" + QString::number(i));
        QLabel* show_player_x = this->findChild<QLabel*>("show_player_" + QString::number(i));
        if(show_p_x->toPlainText() != ""){
            PLAYER temp(show_p_x->toPlainText().toStdString(),100);
            temp.table_sit = i;
            if(i != 8)temp.AI = true;
            gameplay.players.push_back(temp);
            show_player_x->setText(show_p_x->toPlainText());
            show_player_x->show();
            show_p_x->show();
        }

    }
    gameplay.start(gameplay.players,d.deck);
    gameplay.setTurn();
    set_big_or_small_blind();

    set_start();

    ui->text_turn->setText(QString::fromStdString(gameplay.players.at(gameplay.turn).player_name));
    ui->text_pot->setText(QString::number(gameplay.pot));
    evaluation_for_player();
    AI_USE_TURN();
}




