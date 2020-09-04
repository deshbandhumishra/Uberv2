package modelClass

class UberRecordJSON {
   var dt:String=""
   var lat:Any =0D
   var lon:Any =0D
   var base:String=""

  def this(dt: Option[String], lat:Option[Any], lon: Option[Any], base:Option[String])
  {
    this()
    this.dt = dt.getOrElse("")
    this.lat = lat.getOrElse(0D)
    this.lon = lon.getOrElse(0D)
    this.base = base.getOrElse("")
  }

  def getUberRecord: String = this.dt+","+this.lat+","+this.lon+","+this.base
}

/*private[modelClass]
private[modelClass]
private[modelClass]
private[modelClass]*/