package ee.rsx.kata.codurance.atm.money

import ee.rsx.kata.codurance.atm.money.NoteType.BILL
import ee.rsx.kata.codurance.atm.money.NoteType.COIN

enum class Note(val nomination: Int, type: NoteType) {
  BILL_500(500, BILL),
  BILL_200(200, BILL),
  BILL_100(100, BILL),
  BILL_50(50, BILL),
  BILL_20(20, BILL),
  BILL_10(10, BILL),
  BILL_5(5, BILL),
  COIN_2(2, COIN),
  COIN_1(1, COIN)
}

enum class NoteType {
  COIN,
  BILL
}
