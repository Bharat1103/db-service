package org.csw.db.java_api_design

import slick.jdbc.PositionedResult

final class DBRow(delegate: PositionedResult) {
  def nextBoolean(): java.lang.Boolean = delegate.nextBoolean()
  def nextBigDecimal(): java.math.BigDecimal =
    delegate.nextBigDecimal().bigDecimal
  def nextBlob(): java.sql.Blob = delegate.nextBlob()
  def nextByte(): java.lang.Byte = delegate.nextByte()
  def nextBytes(): Array[java.lang.Byte] = delegate.nextBytes().map(Byte.box)
  def nextClob(): java.sql.Clob = delegate.nextClob()
  def nextDate(): java.sql.Date = delegate.nextDate()
  def nextDouble(): java.lang.Double = delegate.nextDouble()
  def nextFloat(): java.lang.Float = delegate.nextFloat()
  def nextInt(): java.lang.Integer = delegate.nextInt()
  def nextLong(): java.lang.Long = delegate.nextLong()
  def nextObject(): java.lang.Object = delegate.nextObject()
  def nextShort(): java.lang.Short = delegate.nextShort()
  def nextString(): java.lang.String = delegate.nextString()
  def nextTime(): java.sql.Time = delegate.nextTime()
  def nextTimestamp(): java.sql.Timestamp = delegate.nextTimestamp()
}
