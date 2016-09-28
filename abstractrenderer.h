#ifndef ABSTRACTRENDERER_H
#define ABSTRACTRENDERER_H


#include <QThread>
#include <QPainter>
#include "page.h"

class AbstractRenderer
{

public:
    AbstractRenderer(QSizeF baseSizeMM);
    ~AbstractRenderer();

    void newPage();


    /**
     * @brief activatePage activate page i
     * @param i
     */
    void activatePage( int i );

    /**
     * @brief currentIndex returns the current index
     * @return
     */
    int currentIndex() const;



    bool isEndlessPage() const;

    Page *currentPage() const;
    int pageCount() const;

public:
    QList<Page*> m_pages;
    int m_currentIndex;
    QSizeF m_baseSizeMM;


    /////////////////////////////////////
    ////
    ///   progress controll
    //
    ////////////////////////////////////
public:
    /**
     * @brief numberOfSteps creating this pdf takes
     *  numberOfSteps() steps.
     * @return
     */
    int numberOfSteps() const;
private:
    int m_currentStep;
protected:
    void notifyCurrentTaskChanged( const QString& message );
signals:
    void progress(int);
    void currentTaskChanged(QString);



    ////////////////////////////////////
    ////
    ///  actual pdf-paint functions
    //
    ///////////////////////////////////
protected:
    // define a convienience struct to abbreviate QList<QList<QPage>> and handle the title
    struct Document
    {
        Document( const QString& title = "" ) : title(title) {}

        QString title;
        QList<Page*> pages;
    };
private:


    void paintTableOfContents();
    void alignSongs( int mode );
    void optimizeForDuplex();
    void decoratePageNumbers();
    int lengthOfSong( int start );


    int m_tableOfContentsPage = -1;
    QStringList m_tableOfContents;

public:
    static const QString HYPHEN;
    static const int ALIGN_SONGS__SEPARATE_PAGES;

    static bool pageBreak(const QStringList & lines, const int currentLine, const double heightLeft, const QPainter *painter );
    static void drawContinueOnNextPageMark(const Page *page, QPainter* painter);

private:
    static QMap<QString, QString> dictionary();
    static void paintHeadline(Page *page, const QString &label);

};

#endif // ABSTRACTRENDERER_H
