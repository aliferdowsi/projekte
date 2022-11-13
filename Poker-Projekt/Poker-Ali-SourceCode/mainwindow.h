#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>

QT_BEGIN_NAMESPACE
namespace Ui { class MainWindow; }
QT_END_NAMESPACE

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    MainWindow(QWidget *parent = nullptr);
    ~MainWindow();
    std::vector<QString>get_player_names();
    void set_fold(QString& name);
    void set_chip(QString& name);
    void set_start();
    void set_end();
    void update_turn();
    void check_round_end();
    void begin_preflop_gui();
    void begin_flop_gui();
    void AI_USE_TURN();
    void begin_turn_gui();
    void begin_river_gui();
    void set_big_or_small_blind();
    void set_evaluation_for_each_player();
    void set_losers_gui();
    void lost_players_remove();
    void table_clean_cards();
    void evaluation_for_player();
    void set_evaluation_none();
    bool AI_raise(int to_raise);
    bool call_AI();
    bool check_AI();
    void fold_AI();

public slots:


    void on_btn_play_clicked();

    void on_btn_player_ok_clicked();


    void on_btn_start_game_clicked();

    void on_btn_sit_6_toggled(bool checked);

    void on_btn_fold_clicked();

    bool on_btn_call_clicked();

    bool on_btn_raise_clicked();



    bool on_btn_check_clicked();

    void on_testbutton_clicked();





private:
    Ui::MainWindow *ui;
};
#endif // MAINWINDOW_H
