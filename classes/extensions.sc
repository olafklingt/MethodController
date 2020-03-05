+Synth{
	*prArgsAsArgArray{|args|
		var parameters, controllerLinks=Set();
		parameters = args.copy.clump(2);
		parameters = parameters.collect { | p |
			var label, controllerOrValue,out;
			#label, controllerOrValue = p;
			if (controllerOrValue.isKindOf(Function)) { controllerOrValue = controllerOrValue.value }; //simply if the parameter is a function
			if(controllerOrValue.isNumber){ // is a number
				out=[label,controllerOrValue]
			}{
				if(controllerOrValue.respondsTo(\linkValueTo)){ //is a controller
					out=[label,controllerOrValue.value]
				}{
					if(controllerOrValue.isArray) { // is one or more controllers that might be transformed
						var  controller, expr;
						#controller, expr = controllerOrValue;
						out=[label,expr.value];
					}{
						Error("Synth Argument Array is not well formed:"+controllerOrValue+"is not an Array, Function, or Controller (should respondsTo(\linkValueTo)").throw;
					}
				}
			};
			out;
		};
		^parameters.flatten(1);
	}

	*prLinkControllersToNode { |array,node|
		var parameters, controllerLinks=Set();
		parameters = array.copy.clump(2);
		parameters.do { | p |
			var label, controllerOrValue;
			#label, controllerOrValue = p;
			if (controllerOrValue.isKindOf(Function)) { controllerOrValue = controllerOrValue.value }; // if the parameter is a function evaluate it
			if(controllerOrValue.respondsTo(\linkValueTo)){ //is a controller
				controllerLinks.add(controllerOrValue);
				controllerOrValue.linkValueTo(node,{|m,val|
					m.value.setn(label,val)
				});
			}{
				if(controllerOrValue.isArray) { // is one or more controllers that might be transformed
					var  controller, expr;
					#controller, expr = controllerOrValue;
					controller.asArray.do { |controller|
						controllerLinks.add(controller);
						controller.linkValueTo(node,{|m,val|
							m.value.setn(label,expr.value)
						});
					}
				}
			}
		};

		if (controllerLinks.size > 0) {
			OSCFunc({controllerLinks.do({ arg c; c.releaseAt(node)})},'/n_end', node.server.addr, nil,[node.nodeID]).oneShot;
		};
	}

	*linkedNew{|defName, args, target, addAction=\addToHead|
		var argArr=this.prArgsAsArgArray(args);
		var synth=Synth(defName, argArr, target, addAction);
		this.prLinkControllersToNode(args,synth);
		^synth;
	}

	linkArgs{|...args|
		Synth.prLinkControllersToNode(args,this);
	}
}

+ ControlSpec {
	makeModel{
		^Ref(this.default)
	}
	makeValueController{
		^MethodController(this.makeModel,spec: this);
	}
}

// + Object {
// 	makeMethodController{|spec=(NonSpec)|
// 		^MethodController(this,spec: spec);
// 	}
	// <-> {|obj,adv|
	// 	^switch(adv,
	// 		\i,{MethodController(this).linkInput(obj)},
	// 		\v,{MethodController(this).linkValue(obj)},
	// 		{MethodController(this).link(obj)}
	// 	)
	// }
// }

// + ArrayedCollection {
// 	makeMethodController{|spec=(NonSpec)|
// 		^this.collect{|item,i|
// 			MethodController.forArray(this,i,spec: spec);
// 		}
// 	}
// }

