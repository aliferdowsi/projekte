#ifndef THREAD_GAMEPLAY_H
#define THREAD_GAMEPLAY_H
#include <QtCore>
#include "vector"
#include "tuple"
#include <string>

using namespace std;

typedef vector<tuple<int,string>> HAND_OR_DECK;

class thread_gameplay : public QThread
{
public:
    thread_gameplay();
    void run();
};



class  PLAYER  {
    public:
        PLAYER(string name,int chipss){
            player_name = name;
            chips = chipss;
        }
        PLAYER(){}
    public:
        HAND_OR_DECK player_hand;
        string player_name;
        tuple<int,int>score = make_tuple(0,0);
        bool raised = false;
        bool all_in = false;
        bool small_blind_paid = false;
        bool is_SMALL_BLIND = false;
        bool is_BIG_BLIND = false;
        bool AI = false;
        int chips = 0;
        int table_sit;
        int raised_amount = 0;
        int round_paid_amount = 0;
        int eval = 0;
        int eval_rank = 0;
        int eval_high_card = 0;
        void Player_add_card(tuple<int,string>&);
        void print_Player_Hand();
        void setTurn(bool Turn);

};



class DECK{
    public:
    vector<tuple<int,string>> deck;
    DECK(){
    };
    public:
    void make_deck();
    void print_deck();
    vector<tuple<int,string>> get_deck();
    int deck_size();


};

typedef enum STAGE {
    preflop,
    flop,
    turn,
    river
} STAGE;

class GAMEPLAY {
    public :
    vector<PLAYER> players;
    vector<PLAYER> folded_players;
    HAND_OR_DECK TABLE;
    DECK gameplay_deck;
    PLAYER last_winner;
    vector<string> ranks = {"HIGH CARD","PAIR","TWO_PAIR","THREE OF A KIND",
    "STRAIGHT","FLUSH","FULL HOUSE","FOUR OF A KIND","STRAIGHT FLUSH","ROYAL FLUSH"};
    vector<PLAYER>lost_players;
    std::vector<int> player_sits;
    void start(vector<PLAYER> &players, HAND_OR_DECK &deck);
    void setTurn();
    void distirbute_card(vector<PLAYER> &Players, int Num_of_cards,HAND_OR_DECK &deck );
    void fold();
    void turn_next();
    void sort_players();
    void AI_USE_TURN();
    bool call();
    bool raise(int raise);
    bool check_all_player_all_in(HAND_OR_DECK& d);
    void set_raised(int player_raised);
    void begin_preflop(HAND_OR_DECK &game_deck);
    void begin_flop(HAND_OR_DECK &game_deck);
    void print_table();
    void round_finished(HAND_OR_DECK &deck);
    bool can_check();
    void change_first_pl_started(int &first_player , bool &started);
    void evaluation(PLAYER &player);
    int calculate_kicker(PLAYER player);
    int is_royal_flush(PLAYER player);
    tuple<int,string> is_flush(PLAYER player);
    int last_round_bigblind = 0;
    int is_straight(PLAYER player);
    int is_straight_flush(PLAYER player);
    int is_four_of_a_kind(PLAYER player);
    int is_three_of_a_kind(PLAYER player);
    int is_fullhouse(PLAYER player);
    int is_pair(PLAYER player);
    int is_high_card(PLAYER player);
    void setBIGBLIND(int new_BigBlind);
    void setSMALLBLIND(int new_SmallBlind);
    tuple<int,int> is_two_pair(PLAYER player);
    PLAYER raised_player;
    STAGE stage;
    bool started = false;
    bool game_finished = false;
    int raised_amount;
    int pot;
    int turn;
    int number_of_players;
    int SMALL_BLIND;
    int SMALL_BLIND_AMOUNT = 5;
    int BIG_BLIND_AMOUNT = 10;
    int BIG_BLIND;
    int first_player;
};

#endif // THREAD_GAMEPLAY_H
