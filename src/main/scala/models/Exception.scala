package models


case class EmEmptyException() extends Exception("em empty")

case class NoFindPicException(s: String) extends Exception(s)
