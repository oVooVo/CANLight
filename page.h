#ifndef PAGE_H
#define PAGE_H

#include <QPicture>
#include <QPainter>
#include <QPageSize>
#include "global.h"

class Page
{
public:
    enum Flag { NothingSpecial, SongStartsHere, TableOfContentsStartsHere, ExistingPage };
    typedef QFlags<Flag> Flags;

    Page(QSizeF baseSizeInMM);
    ~Page();


    QPainter* painter() { return &m_painter; }

    /**
     * @brief picture returns the picture that was painted on.
     *  The recording stops and cannot be continued.
     * @return
     */
    const QPicture &picture();

    QString title() const { return m_title; }

    /**
     * @brief sizeInMM
     * @return the size of the page in mm
     */
    QSizeF sizeInMM() const { return m_sizeInMM; }

    /**
     * @brief sizePainter
     * @return the size of the page in Painter units.
     */
    QSizeF sizePainter() const;

    QRectF contentRect() const;
    QRectF rect() const;

    void growDownMM( double mm );

    double dpi() const;

    static const double MM_INCH;

    double mmInPainterUnits( double mm ) const;
    double painterUnitsInMM( double painter ) const;


    Flags flags() const { return m_flags; }



    // margins in painter-units
    double leftMargin() const { return 35; }
    double rightMargin() const { return 10; }
    double topMargin() const { return 15 + m_additionalTopMargin; }
    double bottomMargin() const { return 15 + 25; } // bottom line is 15 below the end of the page

    void setAdditionalTopMargin(double atm);


private:
    QPicture m_picture;
    QPainter m_painter;
    QSizeF m_sizeInMM;
    Flags m_flags;
    const QString m_title;
    double m_additionalTopMargin;

};

#endif // PAGE_H
