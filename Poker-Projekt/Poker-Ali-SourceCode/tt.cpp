#include "thread_gameplay.h"
#include "mainwindow.h"
#include <QtCore>
#include<QDebug>
#include "stdlib.h"
#include "tuple"
#include<iostream>
#include "random"
#include <algorithm>
#include <QDebug>
#include <typeinfo>

typedef tuple<int,string> CARD;
using namespace std;
vector<string> CARD_TYPES = {"spades","clubs","hearts","diamonds"};

//Function for printing a Card which is  tuple<int,string> CARD;
string print_card(CARD card){
    return to_string(get<0>(card)) + "," + get<1>(card);
}
// Printing a vector of players hand.
string print_players_hands(vector<PLAYER> players){
    string result = "";
    for (PLAYER player : players){
        cout << player.player_name << "'s Hand:"<< endl;
        result += player.player_name + ": ";
        for (CARD card : player.player_hand){
            result +=  print_card(card);
        }
        result += "/n";
    }
    return result;
}

// DECK FUNCTIONS
void DECK::make_deck(){
    vector<tuple<int,string>> result;
    for(int j = 0; j<4;j++){
        for(int i = 2; i<= 14;i++){
            result.push_back(make_tuple(i,CARD_TYPES.at(j)));
        }
    }
    deck = result;

}
void DECK::print_deck(){
    for(int i = 0; i< size(deck); i++){
        tuple temp = deck.at(i);
        print_card(temp);
        //cout << get<0>(temp) << "of " << get<1>(temp)  <<  endl;
    }
}





int DECK::deck_size(){
    return size(deck);
}

vector<tuple<int,string>> DECK::get_deck(){
    return deck;
}

// PLAYER FUNCTIONS

void PLAYER::Player_add_card(CARD &card){
    player_hand.push_back(card);
}

void PLAYER::print_Player_Hand(){
    for(int i = 0; i<size(player_hand);i++){
        print_card(player_hand.at(i));
    }
}

// Game Play Functions 0123
void GAMEPLAY::set_raised(int player_raised){
    for(int i = 0; i< players.size();i++){
        if ( i != player_raised){
            players.at(i).raised = true;
        }
    }
}

void GAMEPLAY::setTurn(){
    int s_turn;
    int b_turn;
    if ( players.size() == 1 || players.size() == 0 ){
        game_finished = true;
        exit(1);
        return;
    }


    turn = 0+ last_round_bigblind;
    for(int i=0;i<players.size();i++){
        PLAYER& player = players.at(i);
        player.is_BIG_BLIND = false;
        player.is_SMALL_BLIND = false;
        player.small_blind_paid = false;
        player.round_paid_amount = 0;
    }
    if(turn-2 < 0){
        players.at(number_of_players-2+last_round_bigblind).is_SMALL_BLIND = true;
        players.at(number_of_players-2+last_round_bigblind).round_paid_amount = SMALL_BLIND_AMOUNT;
        players.at(number_of_players-2+last_round_bigblind).chips -= SMALL_BLIND_AMOUNT;
    } else {
        players.at(turn-2).is_SMALL_BLIND = true;
        players.at(turn-2).round_paid_amount = SMALL_BLIND_AMOUNT;
        players.at(turn-2).chips -= SMALL_BLIND_AMOUNT;
    }
    if(turn-1 < 0){
        players.at(number_of_players-1).is_BIG_BLIND = true;
        players.at(number_of_players-1).small_blind_paid = true;
        players.at(number_of_players-1).round_paid_amount = BIG_BLIND_AMOUNT;
        players.at(number_of_players-1).chips -= BIG_BLIND_AMOUNT;
    } else {
        players.at(turn-1).is_BIG_BLIND = true;
        players.at(turn-1).small_blind_paid = true;
        players.at(turn-1).round_paid_amount = BIG_BLIND_AMOUNT;
        players.at(turn-1).chips -= BIG_BLIND_AMOUNT;
    }
    pot += SMALL_BLIND + BIG_BLIND_AMOUNT;
    first_player = turn;
    stage = preflop;
    last_round_bigblind++;
    if(last_round_bigblind == players.size()){
        last_round_bigblind = 0;
    }

}

bool GAMEPLAY::raise(int raise){

    if ( players.at(turn).chips < raise || players.at(turn).chips <= 0 || raise <= 0 ){
        cout << "NOT ENOUGH CHIPS TO RAISE" << raise << " AMOUNT." <<endl;
        return false;
    } else if ( players.at(turn).raised == true && raise <= raised_amount){
        cout << "Amount raise ("<<raise<<") cannot be equal or less than the raised amount "<< raised_amount<<endl;
        return false;
    } else if ( raise < BIG_BLIND_AMOUNT && players.at(turn).chips > BIG_BLIND_AMOUNT){
        cout << "Raise amount must be higher than Big Blind Amount";
        return false;
    }
    players.at(turn).raised_amount = raise;
    raised_amount = raise;

    if(players.at(turn).raised){
        players.at(turn).chips  = players.at(turn).chips - raise + players.at(turn).round_paid_amount;
        players.at(turn).round_paid_amount = raise;
        first_player = turn;
        pot += raise;
        set_raised(turn);
        raised_player = players.at(turn);
        turn_next();
    } else {
        players.at(turn).chips -= raise;
        players.at(turn).round_paid_amount = raise;
        first_player = turn;
        pot += raise;
        set_raised(turn);
        raised_player = players.at(turn);
        if(players.at(turn).chips == 0)players.at(turn).all_in=true;
        turn_next();
    }
    return true;
}

bool GAMEPLAY::check_all_player_all_in(HAND_OR_DECK& deck){
    for (PLAYER player : players){
        if ( player.chips != 0 )return false;
    }

    return true;
}
void GAMEPLAY::fold(){
    // 0 1 2 3
    // 0 1 2
    if ( turn == number_of_players-1 ){
        printf("1,fold");
        first_player == turn -1;
        number_of_players--;
        folded_players.push_back(players.at(turn));
        players.at(turn).player_hand.clear();
        players.erase(players.begin()+turn);
        turn = 0;
    } else{
        if (first_player)first_player--;
        number_of_players--;
        folded_players.push_back(players.at(turn));
        players.at(turn).player_hand.clear();
        players.erase(players.begin()+turn);
        printf("3,fold");
    }
    if ( number_of_players == 0 || number_of_players == 1 ){
        std::cout<< "ROUND FINISHasdasdED";
    }
}
bool GAMEPLAY::call(){
    PLAYER &player = players.at(turn);

    if(player.chips + player.round_paid_amount < raised_amount ){
        pot += player.chips;
        player.round_paid_amount += player.chips;
        player.all_in = true;
        turn_next();
        player.chips = 0;
        return true;
    }
    if(player.small_blind_paid == false){
        if(player.raised == false){ // Player not raised and not paid big blind
            int call_bb = BIG_BLIND_AMOUNT - player.round_paid_amount;
            player.chips -= call_bb;
            pot += call_bb;
            player.round_paid_amount += call_bb;
            player.small_blind_paid = true;
            if(player.chips < 0)player.chips = 0;
            turn_next();
            return true;
        }
    } else { // BIG BLIND PAID
        if (player.raised == false){
            return false;
        }
    }
    player.chips -= raised_amount - player.round_paid_amount;
    pot += raised_amount - player.round_paid_amount;
    player.round_paid_amount += raised_amount - player.round_paid_amount;
    if(player.chips < 0)player.chips = 0;
    turn_next();
    return true;
}
/*
random_device rand;
mt19937 gen(rand());
while (Num_of_cards) {
    for(PLAYER &pl : players){
        uniform_int_distribution<int> distr(0,(int)game_deck.size()-1);
        int index = distr(gen);
*/


void GAMEPLAY::turn_next(){
    if ( turn == number_of_players-1){
        turn = 0;
    } else {
        turn++;
    }
    // check if player is actually AI


}
bool GAMEPLAY::can_check(){
    if(players.at(turn).small_blind_paid == false || players.at(turn).raised == 1){
        return false;
    } else return true;
}

// Distrbutes Num_of_cards amount from a deck to a vector of players
int countl = 0;
void GAMEPLAY::distirbute_card(vector<PLAYER> &Players, int Num_of_cards,HAND_OR_DECK &game_deck ){

    random_device rand;
    mt19937 gen(rand());
    while (Num_of_cards) {
        for(PLAYER &pl : players){
            uniform_int_distribution<int> distr(0,(int)game_deck.size()-1);
            int index = distr(gen);
            pl.player_hand.push_back(game_deck.at(index));
            game_deck.erase(game_deck.begin()+index);
        }
        Num_of_cards--;
    }

    countl++;
}



void GAMEPLAY::print_table(){
    for ( CARD a : TABLE ){
        print_card(a);
    }
}

// Function at the start of flop where table gets 3 random cards from deck.
void GAMEPLAY::begin_preflop(HAND_OR_DECK &game_deck){
    random_device rand;
    mt19937 gen(rand());
    uniform_int_distribution distr(0,(int)game_deck.size()-1);
    vector<PLAYER> &playerss = players;

    for (int i = 0; i<3;i++){
        uniform_int_distribution distr(0,(int)game_deck.size()-1);
        int index = distr(gen);
        TABLE.push_back(game_deck.at(index));
        game_deck.erase(game_deck.begin()+index);
    }
    for (PLAYER &player : playerss){
        player.raised_amount = 0;
        player.round_paid_amount = 0;
        player.raised = 0;
        player.small_blind_paid = 1;
    }
    first_player = number_of_players-2;
    turn = number_of_players-2;
    started = 0;

}


void GAMEPLAY::begin_flop(HAND_OR_DECK &game_deck){
    random_device rand;
    mt19937 gen(rand());
    uniform_int_distribution distr(0,(int)game_deck.size()-1);
    vector<PLAYER> &playerss = players;
    for (int i = 0; i<1;i++){
        uniform_int_distribution distr(0,(int)game_deck.size()-1);
        int index = distr(gen);
        TABLE.push_back(game_deck.at(index));
        game_deck.erase(game_deck.begin()+index);
    }
    for (PLAYER &player : playerss){
        player.raised_amount = 0;
        player.round_paid_amount = 0;
        player.raised = 0;
        player.small_blind_paid = 1;
    }
    first_player = number_of_players-2;
    turn = number_of_players-2;
    started = 0;

}

// Start of the game where each player gets 2 cards.
void GAMEPLAY::start(vector<PLAYER> &Players,HAND_OR_DECK &deck ){
    distirbute_card(Players,2,deck);
    number_of_players = Players.size();
    turn = 0;
}

void GAMEPLAY::setBIGBLIND(int newblind){
    BIG_BLIND_AMOUNT = newblind;
}

void GAMEPLAY::setSMALLBLIND(int newblind){
    SMALL_BLIND_AMOUNT = newblind;
}


void gameplay_print(GAMEPLAY &gameplay, PLAYER &player){
        if ( gameplay.stage != preflop){
            cout << "TABLE: \n";
            gameplay.print_table();
        }
        cout<<"\nPOT: "<<gameplay.pot<< "\nIt is "<<player.player_name<<"'s Turn. ( " //Current player info print
        << player.chips << " )";

        if (player.is_BIG_BLIND == true){
            cout<< " ( BIGBLIND )\n";
        } else if ( player.is_SMALL_BLIND == true){
            cout<< " ( SMALLBLIND )\n";
        }else {
            cout<<"\n";
        }
        player.print_Player_Hand();
}
//<< >>
tuple<int,string> GAMEPLAY::is_flush(PLAYER player){
    HAND_OR_DECK hand = player.player_hand;
    int count = 0;
    string result;
    vector<int> res;
    for (string a : CARD_TYPES){
        vector<int> temp;
        for (CARD card : hand ){
            if (get<1>(card) == a){
                temp.push_back(get<0>(card));
                count++;
            }
        }
        if ( count  >= 5 ){
            result = a;
            res = temp;
            break;
        }
        count = 0;
    }
    //{"SPADE","CLUBS","HEART","DIAMOND"};
    if (res.size()){
        return make_tuple(*max_element(res.begin(),res.end()),result);
    }
    return make_tuple(0,"");
}

//          {"SPADE","CLUBS","HEART","DIAMOND"};
//flush_type   1        2        3       4     encoded.
int GAMEPLAY::is_straight_flush(PLAYER player){
    int lowest = is_straight(player);
    tuple<int,string> flush_type = is_flush(player);
    int result = 0;
    if ( lowest && get<0>(flush_type) ){
        string type = get<1>(flush_type);
        for(int i = lowest; i < lowest+5; i++){
            result += count_if(player.player_hand.begin(), player.player_hand.end(), [&](const CARD& e) {return (get<1>(e) == type && get<0>(e)==i);});
        }
    }
    if (result >= 5){
        return lowest;
    }
    return 0;
}
int GAMEPLAY::is_royal_flush(PLAYER player){
    if (is_straight_flush(player) == 10){
        return 1;
    }
    return 0;
}
int GAMEPLAY::is_four_of_a_kind(PLAYER player){
    HAND_OR_DECK hand = player.player_hand;
    int result = 0;
    for(CARD card : hand){
        int num = get<0>(card);
        result = count_if(player.player_hand.begin(), player.player_hand.end(), [&](const CARD& e) {return (get<0>(e) == num);});
        if ( result >= 4){
            return num;
        }
    }
    return 0;
}

int GAMEPLAY::is_three_of_a_kind(PLAYER player){
    HAND_OR_DECK hand = player.player_hand;
    int result = 0;
    for(CARD card : hand){
        int num = get<0>(card);
        result = count_if(player.player_hand.begin(), player.player_hand.end(), [&](const CARD& e) {return (get<0>(e) == num);});
        if ( result == 3){
            return num;
        }
    }
    return 0;
}
int GAMEPLAY::is_pair(PLAYER player){
    HAND_OR_DECK hand = player.player_hand;
    int result = 0;
    for(CARD card : hand){
        int num = get<0>(card);
        result = count_if(player.player_hand.begin(), player.player_hand.end(), [&](const CARD& e) {return (get<0>(e) == num);});
        if ( result == 2){
            return num;
        }
    }
    return 0;
}
int GAMEPLAY::is_fullhouse(PLAYER player){
    int result = is_three_of_a_kind(player);
    if ( result && is_pair(player)) return result;
    return 0;
}
tuple<int,int> GAMEPLAY::is_two_pair(PLAYER player){
    HAND_OR_DECK hand = player.player_hand;
    vector<int> result;
    int count = 0;
    for(CARD card : hand){
        int num = get<0>(card);
        count = count_if(hand.begin(), hand.end(), [&](const CARD& e) {return (get<0>(e) == num);});
        if ( count == 2){
            result.push_back(num);
        }
    }
    int size = result.size();
    if (size >2){
        sort(result.begin(),result.end());
        result.erase(std::unique(result.begin(),result.end()), result.end());
        return make_tuple(result.at(result.size()-1),result.at(result.size()-2));
    }

    return make_tuple(0,0);
}
int GAMEPLAY::is_high_card(PLAYER player){
    int max = 0;
    for ( CARD card : player.player_hand){
        if ( get<0>(card)> max) max = get<0>(card);
    }
    return max;
}

int GAMEPLAY::is_straight(PLAYER player){ //Check if the player has a straight. Return value is the lowest value card of the straight.
    HAND_OR_DECK hand = player.player_hand;
    std::vector<int> result;
    for (int i = 0; i < hand.size(); i++ ){
        CARD card = hand.at(i);
        int num = get<0>(card);
        int count = 0;
        for (int j = num+1; j < num+5; j++ ){
            for(CARD card : hand){
                if (j == get<0>(card)){
                    count++;
                    break;
                }
            }
        }
        if (count == 4) result.push_back(num);
    }
    if (result.size()){
        return *max_element(result.begin(),result.end());
    }
    return 0;
}


int GAMEPLAY::calculate_kicker(PLAYER player){
    int max = 0;
    int eval = player.eval;
    int eval_rank = player.eval_rank;
    if(eval == 10 || eval == 9 || eval == 7 || eval == 6 || eval == 5)return eval_rank;

    for ( CARD card : player.player_hand){
        if ( get<0>(card)> max &&  (int)(get<0>(card)) != eval_rank ){
            max = get<0>(card);
        }
    }
    return max;
}
// Royal Flush, Straight Flush, Four of a Kind, Full House, Flush , Straight, Three of a Kind, Two Pair, Pair, Highcard
//     10     ,        9      ,        8       ,     7    ,   6    ,    5    ,      4        ,      3   ,  2  ,      1
void GAMEPLAY::evaluation(PLAYER &player){
    PLAYER p = player;
    for(CARD card : TABLE) p.player_hand.push_back(card);
    if (int rank = is_royal_flush(p)){
        player.eval = 10;
        player.eval_rank = rank;
        return;
    } else if (int rank =is_straight_flush(p)){
        player.eval = 9;
        player.eval_rank = rank;
        return;
    } else if (int rank =is_four_of_a_kind(p)){
        player.eval = 8;
        player.eval_rank = rank;
        return;
    } else if (int rank =is_fullhouse(p)){
        player.eval = 7;
        player.eval_rank = rank;
        return;
    } else if (int rank =get<0>(is_flush(p))){
        player.eval = 6;
        player.eval_rank = rank;
        return;
    } else if (int rank =is_straight(p)){
        player.eval = 5;
        player.eval_rank = rank;
        return;
    } else if (int rank =is_three_of_a_kind(p)){
        player.eval = 4;
        player.eval_rank = rank;
        return;
    } else if (int rank =get<0>(is_two_pair(p))){
        player.eval = 3;
        player.eval_rank = rank;
        return;
    } else if (int rank =is_pair(p)){
        player.eval = 2;
        player.eval_rank = rank;
        return;
    } else if (int rank =is_high_card(p)){
        player.eval = 1;
        player.eval_rank = rank;
        return;
    }

}


// Royal Flush, Straight Flush, Four of a Kind, Full House, Flush , Straight, Three of a Kind, Two Pair, Pair, Highcard
//     10     ,        9      ,        8       ,     7    ,   6    ,    5    ,      4        ,      3   ,  2  ,      1

void GAMEPLAY::sort_players(){
    std::sort(players.begin(),players.end(),[&](const PLAYER& a, const PLAYER& b){
        return a.table_sit < b.table_sit;
    });
}

void GAMEPLAY::round_finished(HAND_OR_DECK&deck){

    PLAYER Winner;
    int winner_turn = 0;
    int winner_eval = 0;
    int winner_eval_rank = 0;
    int winner_high_card = 0;
    int count = 0;
    for(int i = 0; i <players.size();i++){ // Finds the winner evaluation value.
        evaluation(players.at(i));
        cout<< players.at(i).player_name << ": "<<ranks.at(players.at(i).eval - 1)<<","<< players.at(i).eval_rank << " - CHIP: "<<players.at(i).chips<< endl;
        if (players.at(i).eval > winner_eval) {
                Winner = players.at(i);
                winner_turn = i;
                winner_eval = players.at(i).eval;
                winner_eval_rank = players.at(i).eval_rank;
        } else if ( players.at(i).eval == winner_eval ){
            if (players.at(i).eval_rank > winner_eval_rank ) {
                Winner = players.at(i);
                winner_turn = i;
                winner_eval = players.at(i).eval;
                winner_eval_rank = players.at(i).eval_rank;
                continue;
            }else if (players.at(i).eval_rank == winner_eval_rank){
                int k = calculate_kicker(players.at(i));
                if ( k > winner_high_card ) {
                    Winner = players.at(i);
                    winner_turn = i;
                    winner_eval = players.at(i).eval;
                    winner_eval_rank = players.at(i).eval_rank;
                    winner_high_card = k;
                    continue;
                }else if (is_high_card(players.at(i)) ==  winner_high_card ){
                    // NOT IMPLEMENTED YET.
                }
            }
        }
    }

    players.at(winner_turn).chips += pot;
    raised_amount = 0;
    pot = 0;
    print_table();
    qDebug() << "\nROUND FINISHED\n";
    last_winner = Winner;
    qDebug() << "1";
    for (int i = 0; i< players.size();i++){
        PLAYER &player = players.at(i);
        player.raised_amount = 0;
        player.round_paid_amount = 0;
        player.raised = 0;
        player.small_blind_paid = false;
        player.player_hand.clear();
        player.all_in = false;
        cout<< player.player_name << ": "<<ranks.at(player.eval - 1)<<","<< player.eval_rank << " - CHIP: "<<player.chips<< endl;
    }
    qDebug() << "1";
    for (PLAYER& p : folded_players){
        p.raised_amount = 0;
        p.round_paid_amount = 0;
        p.raised = 0;
        p.small_blind_paid = false;
        p.player_hand.clear();
        p.all_in = false;
        players.push_back(p);
    }
    qDebug() << "1";
    folded_players.clear();
    for(int i=0; i<players.size();i++){
        if ( players.at(i).chips <= 0 ){
            lost_players.push_back(players.at(i));
            players.erase(players.begin()+i);
            i--;
            continue;
        }
        PLAYER& p = players.at(i);
        p.player_hand.clear();
    }
    qDebug() << "1";
    sort_players();
    TABLE.clear();
    gameplay_deck.deck.clear();
    gameplay_deck.make_deck();
    start(players,gameplay_deck.deck);

    first_player = 0;
    turn = 0;
    started = 0;
    stage = preflop;
}
