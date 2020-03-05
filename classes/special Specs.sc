FuncSpec{
	var <>mapFunc,<>unmapFunc,<>constrainFunc,<>defaultFunc;

	*new{arg mapFunc={|v|v},unmapFunc={|v|v},constrainFunc={|v|v},defaultFunc={0};
		^super.newCopyArgs(mapFunc,unmapFunc,constrainFunc,defaultFunc);
	}

	map { | v | ^mapFunc.value(v) }
	unmap { | v | ^unmapFunc.value(v) }
	constrain { | v | ^constrainFunc.value(v) }
	asSpec { ^this }
	default { ^defaultFunc.value }
}

NonSpec{
	*map { | v | ^v  }
	*unmap { | v | ^v }
	*constrain { | v | ^v }
	*asSpec { ^this }
	*default { ^0 }
}