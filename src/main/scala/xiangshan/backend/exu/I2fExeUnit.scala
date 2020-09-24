package xiangshan.backend.exu

import chisel3._
import chisel3.util._
import xiangshan.backend.fu.fpu.IntToFloatSingleCycle
import xiangshan.backend.fu.fpu.FPUOpType._

class I2fExeUnit extends Exu(Exu.i2fExeUnitCfg){

  val uopIn = io.in.bits.uop
  val isDouble = !uopIn.ctrl.isRVF
  val fuOp = uopIn.ctrl.fuOpType
  val fu = fuOp.head(3)
  val op = fuOp.tail(3)
  val valid = io.in.valid && !uopIn.needFlush(io.redirect)
  val intToFloat = Module(new IntToFloatSingleCycle)
  val extraInput = intToFloat.io.in.bits.ext.get
  extraInput.isDouble := isDouble
  extraInput.rm := DontCare
  extraInput.op := op
  intToFloat.io.in.valid := valid && fu===("b"+FU_I2F).U
  intToFloat.io.in.bits.src(0) := io.in.bits.src1
  intToFloat.io.in.bits.uop := uopIn
  intToFloat.io.redirectIn := io.redirect
  io.out.valid := valid
  io.out.bits.data := Mux(intToFloat.io.out.valid,
    intToFloat.io.out.bits.data,
    io.in.bits.src1
  )
  io.out.bits.uop := uopIn
  io.out.bits.redirect <> DontCare
  io.out.bits.redirectValid := false.B
  io.out.bits.debug <> DontCare
}
