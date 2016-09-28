#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>

namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

private slots:
    void on_pushButtonBackToList_clicked();
    void on_buttonTransposeDown_clicked();
    void on_buttonTransposeUp_clicked();
    void on_buttonPDF_clicked();
    void addItem(const QString &text = "", const QString &pattern = "");
    void on_listWidget_clicked(const QModelIndex &index);
    void on_pushButtonBackToEdit_clicked();
    void on_pushButtonSavePdf_clicked();
    void on_pushButtonPrint_clicked();
    void save();
    void load();
    void exportToFile();
    void importFromFile();
    void on_buttonImport_clicked();

    void on_pushButton_2_clicked();

protected:
    bool event(QEvent *event);

private:
    Ui::MainWindow *ui;
    QStringList m_patterns;
    int m_currentRow;

    QJsonDocument toJson() const;
    void fromJson(const QJsonDocument& json);
};

#endif // MAINWINDOW_H
