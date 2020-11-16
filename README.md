# PDFViewPager-with-thumbnails

Downloads PDF from internet using [PrDownloader](https://github.com/MindorksOpenSource/PRDownloader).
[PdfViewPager](https://github.com/voghDev/PdfViewPager) converts it to Viewpager.

Pdf is converted to List<Bitmaps?> and passed to Recyclerview which acts as thumbnails for viewpager.

PageChangeListener is attached to PDFviewpager and onClickListener is attached to recyclerview to make both work together.

