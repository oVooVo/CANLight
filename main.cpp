#include "mainwindow.h"
#include <QApplication>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);

    a.setApplicationName("CANLight");
    a.setOrganizationDomain("CAN Developers");

    MainWindow w;
    w.grabGesture(Qt::TapGesture);
    w.grabGesture(Qt::TapAndHoldGesture);
    w.show();

    return a.exec();
}
