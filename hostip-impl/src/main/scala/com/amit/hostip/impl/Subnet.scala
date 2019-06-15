package com.amit.hostip.impl

import java.math.BigInteger
import java.net.InetAddress
import java.nio.ByteBuffer

import play.api.Logger

trait Subnet {
  def isInRange(ipAddress: InetAddress): Boolean
}

private sealed class SubnetImpl(cidr: String) extends Subnet {

  val logger = Logger(this.getClass)

  private val (address, network) = cidr.split("/", 2).toList match {
    case a :: n :: Nil ⇒ InetAddress.getByName(a) → Integer.valueOf(n)
    case _ ⇒ throw new IllegalArgumentException("Invalid CIDR format")
  }

  def isInRange(address: InetAddress): Boolean = {
    val start: BigInteger = new BigInteger(1, this.startAddress.getAddress)
    val end: BigInteger = new BigInteger(1, this.endAddress.getAddress)
    val target: BigInteger = new BigInteger(1, address.getAddress)
    val st: Int = start.compareTo(target)
    val te: Int = target.compareTo(end)
    (st == -1 || st == 0) && (te == -1 || te == 0)
  }

  private def calculate(): (InetAddress, InetAddress) = {
    var maskBuffer: ByteBuffer = null
    var targetSize: Int = 0
    if (address.getAddress.length == 4) {
      maskBuffer = ByteBuffer.allocate(4).putInt(-1)
      targetSize = 4
    }
    else {
      maskBuffer = ByteBuffer.allocate(16).putLong(-1L).putLong(-1L)
      targetSize = 16
    }
    val mask: BigInteger = new BigInteger(1, maskBuffer.array).not.shiftRight(network)
    val buffer: ByteBuffer = ByteBuffer.wrap(address.getAddress)
    val ipVal: BigInteger = new BigInteger(1, buffer.array)
    val startIp: BigInteger = ipVal.and(mask)
    val endIp: BigInteger = startIp.add(mask.not)
    val startIpArr: Array[Byte] = toBytes(startIp.toByteArray, targetSize)
    val endIpArr: Array[Byte] = toBytes(endIp.toByteArray, targetSize)
    InetAddress.getByAddress(startIpArr) → InetAddress.getByAddress(endIpArr)
  }

  private def toBytes(array: Array[Byte], targetSize: Int): Array[Byte] = {
    if (array.length > targetSize) array.drop(array.length - targetSize)
    else Array.fill[Byte](targetSize - array.length)(0) ++ array
  }

  val (startAddress, endAddress) = calculate()
}

object Subnet {

  val logger = Logger(this.getClass)

  @throws[IllegalArgumentException]("if invalid CIDR provided")
  def apply(cidr: String): Subnet = new SubnetImpl(cidr)

  private def dotIp(ip : Long) : String = {
    val bytes: Array[Byte] = new Array[Byte](4)
    bytes(0) = ((ip & 0xff000000) >> 24).toByte
    bytes(1) = ((ip & 0x00ff0000) >> 16).toByte
    bytes(2) = ((ip & 0x0000ff00) >> 8).toByte
    bytes(3) = (ip & 0x000000ff).toByte
    InetAddress.getByAddress(bytes).getHostAddress()
  }

  def getGoodBad(ips:Seq[Long]) :(Seq[Long],Seq[Long])= {
    ips.partition{ x =>
      val ipStr = dotIp(x)
      Subnet(s"${ipStr}/28").isInRange(InetAddress.getByName(ipStr))
    }
  }
}