#-------------------------------------------------
#
# Project created by QtCreator 2016-09-27T20:32:06
#
#-------------------------------------------------

QT       += core gui printsupport network xml

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = CANLight
TEMPLATE = app

QMAKE_CXXFLAGS += -std=c++0x


SOURCES += main.cpp\
        mainwindow.cpp \
    chordpatternedit.cpp \
    chord.cpp \
    looselines.cpp \
    util.cpp \
    pdfview.cpp \
    abstractrenderer.cpp \
    page.cpp \
    importer.cpp \
    importdialog.cpp \
    abstractnetworkreceiver.cpp \
    contentnetworkreceiver.cpp

HEADERS  += mainwindow.h \
    chordpatternedit.h \
    chord.h \
    looselines.h \
    global.h \
    util.h \
    pdfview.h \
    abstractrenderer.h \
    page.h \
    importer.h \
    importdialog.h \
    abstractnetworkreceiver.h \
    contentnetworkreceiver.h

FORMS    += mainwindow.ui \
    pdfview.ui \
    importdialog.ui
