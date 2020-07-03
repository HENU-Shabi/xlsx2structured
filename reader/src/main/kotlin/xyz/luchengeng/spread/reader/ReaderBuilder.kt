package xyz.luchengeng.spread.reader

class ReaderBuilder : AbstractBuilder<SpreadSheetReader>() {
    override fun build() : SpreadSheetReader{
        return SpreadSheetReader(this.rawStatements)
    }
}