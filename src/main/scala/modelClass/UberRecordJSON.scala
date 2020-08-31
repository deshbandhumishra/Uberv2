package modelClass

class UberRecordJSON {
  private[modelClass] var dt=""
  private[modelClass] var lat =0D
  private[modelClass] var lon =0D
  private[modelClass] var base=""

  def this(dt: String, lat: Double, lon: Double, base: String) {
    this()
    this.dt = dt
    this.lat = lat
    this.lon = lon
    this.base = base
  }

  def getUberRecord: String = this.dt+","+this.lat+","+this.lon+","+this.base
}

