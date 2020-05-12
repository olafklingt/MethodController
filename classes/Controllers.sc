SuperClassOfObjectAndMethodController : Stream{
	var <model, <msg, <modelSpec;
	var <modelChangedActionDictionary;
	var <objChangedActionDictionary;
	replaceModel{ | newModel |
		this.remove;
		model = newModel;
		newModel.addDependant( this );
		this.updateLinks;
	}

	update { arg theChanger, what;
		if( theChanger === model ){
			if( what === msg ){
				modelChangedActionDictionary.do{ | a | a.value( theChanger ) };
			}
		}{
			objChangedActionDictionary[theChanger].value( theChanger );
		}
	}

	prLinkTo{ | obj, linkfunc |
		if( modelChangedActionDictionary[obj].isNil ){
			modelChangedActionDictionary[obj] = FunctionList( )
		};

		modelChangedActionDictionary[obj].addFunc( linkfunc );

		model.addDependant( this );


		// to make it compatible with SCViewHolder i use try

		// [obj,obj.class,oPbj.respondsTo( \onClose_ ) ].debug(\oroc_);
		// if( obj.respondsTo( \onClose_ ) ){ // if obj is a View remove on close
		// 	if( obj.onClose.isNil ){
		// 		obj.onClose = FunctionList( );
		// 	};
		// 	obj.onClose.addFunc{ this.unlink( obj.debug(\unlinkthis) ) };
		// }{
		(try{
			if( obj.onClose.isNil ){
				obj.onClose = FunctionList( );
			}{
				if(obj.onClose.class==Function){
					var fl=FunctionList( );
					obj.onClose = FunctionList( );
					obj.onClose.addFunc(fl);
				}
			};
			obj.onClose.addFunc{ this.unlink( obj ) };
			false;// no error
		}?true).if{
			if(obj.class.asString.contains("View")){
			("View Class is not well implemented it should implement onClose"++obj.asCompileString).warn;
			}
		};
		this.updateLinks;
	}

	linkInputTo{ | obj, objset = \value_, spec = ( NonSpec ) |
		var linkfunc;
		spec = spec.asSpec;
		linkfunc = switch( objset.class,
			Array, `{ obj.perform( *( objset++[spec.map( this.input )] ) ) },
			Function, `{ objset.value( obj, spec.map( this.input ), this ) },
			`{ obj.perform( objset, spec.map( this.input ) ) }
		);
		this.prLinkTo( obj, linkfunc );
	}

	linkValueTo{ | obj, objset = \value_, spec = ( NonSpec ) |
		var linkfunc;
		spec = spec.asSpec;
		linkfunc = switch( objset.class,
			Array, `{ obj.perform( *( objset++[spec.map( this.value )] ) ) },
			Function, `{ objset.value( obj, spec.map( this.value ), this ) },
			`{ obj.perform( objset, spec.map( this.value ) ) }
		);
		this.prLinkTo( obj, linkfunc );
	}

	input {
		^modelSpec.unmap( this.value )
	}

	remove {
		model.removeDependant( this );
	}

	unlink{ | obj |
		objChangedActionDictionary.removeAt( obj );
		obj.removeDependant( this );
		modelChangedActionDictionary.removeAt( obj );
		if( modelChangedActionDictionary.size === 0 ){
			this.remove;
		}
	}

	next { ^this.value }
}

MethodController : SuperClassOfObjectAndMethodController{
	var <modelget, <modelset, <>updateObjectOnChange;
	*forArray{ | model, pos, spec = ( NonSpec ), updateObjectOnChange = true, msg |
		^MethodController.new( model, [\at, pos], [\put, pos], spec.asSpec, updateObjectOnChange, msg )
	}

	*new{ | model, modelget, modelset, spec = ( NonSpec ), updateObjectOnChange = true, msg |
		modelget = modelget?\value;
		modelset = modelset??{ ( modelget ).asSetter };
		if( msg.isSymbol.not ){
			if( modelget.isSymbol ){
				msg = msg?modelget;
			}{
				if( modelget.isArray ){
					var m = "";
					modelget.do{ | e |
						m = m++"_"++e;
					};
					msg = msg?m.asSymbol;
				}{
					msg = \methodControllerSync;
				}
			}
		};
		^super.newCopyArgs( model
			, msg
			, spec.asSpec
			, IdentityDictionary( )
			, IdentityDictionary( )
			, modelget
			, modelset
			, updateObjectOnChange
		);
	}

	prLinkFrom{ | obj, linkfunc |
		if( objChangedActionDictionary[obj].isNil ){
			objChangedActionDictionary[obj] = FunctionList( )
		};
		objChangedActionDictionary[obj].addFunc( linkfunc );
		obj.addDependant( this );

		// if( obj.respondsTo( \action ).not ) { // if obj doesn't have a action hook ( is not a View )
		// 	"linked Object should have a action hook otherwise it only get called if obj.changed( \\viewGotUpdated ); is called".warn;
		// }{
		// 	obj.action = { | obj |
		// 		// [this,obj,\viewGotUpdated].debug;
		// 		obj.changed( \viewGotUpdated );
		// 	}
		// }

		// also to make SCViewHolder more compatible
		(try{
			obj.action = { | obj |
				obj.changed( \viewGotUpdated );
			};
			false;// no error
		}?true).if{
			("linked Object should have a action hook otherwise it only get called if obj.changed( \\viewGotUpdated ); is called:"++obj.asCompileString).warn;
		};
	}

	linkInputFrom{ | obj, objget = \value, spec = ( NonSpec ) |
		var linkfunc;
		spec = spec.asSpec;
		linkfunc = switch( objget.class,
			Array, `{ this.input = spec.unmap( obj.perform( *objget ) ) },
			Function, `{ this.input = spec.unmap( objget.value( obj ) ) },
			`{ this.input = spec.unmap( obj.perform( objget ) ) }
		);
		this.prLinkFrom( obj, linkfunc );
	}

	linkValueFrom{ | obj, objget = \value, spec = ( NonSpec ) |
		var linkfunc;
		spec = spec.asSpec;
		linkfunc = switch( objget.class,
			Array, `{ this.value = spec.unmap( obj.perform( *objget ) ) },
			Function, `{ this.value = spec.unmap( objget.value( obj ) ) },
			`{ this.value = spec.unmap( obj.perform( objget ) ) }
		);
		this.prLinkFrom( obj, linkfunc );
	}

	link{ | obj, objget = \value, objset, spec = ( NonSpec ) |
		this.linkInput( obj, objget, objset, spec.asSpec );
	}

	linkInput{ | obj, objget = \value, objset, spec = ( NonSpec ) |
		objset = objset??{ ( objget ).asSetter };
		this.linkInputTo( obj, objset, spec.asSpec );
		this.linkInputFrom( obj, objget, spec.asSpec );
	}

	linkValue{ | obj, objget = \value, objset, spec = ( NonSpec ) |
		objset = objset??{ ( objget ).asSetter };
		this.linkValueTo( obj, objset, spec.asSpec );
		this.linkValueFrom( obj, objget, spec.asSpec );
	}

	input_ { | val |
		this.value_( modelSpec.map( val ) );
	}

	value{
		var v;
		v = switch( modelget.class,
			Array, { model.perform( *modelget ) },
			Function, { modelget.value( model ) },
			{ model.perform( modelget ) }
		);
		^v;
	}

	value_{ | val |
		switch( modelset.class,
			Array, { model.perform( *( modelset++[val] ) ) },
			Function, { modelset.value( model, val, this ) },
			{ model.perform( modelset, val ) }
		);
		this.updateLinks;
	}

	updateLinks {
		model.changed( msg );
		if( updateObjectOnChange ) {
			var d = model.dependants;
			d.select{ | a | a.class == ObjectController }
			.collect{ | oc | oc.msg }
			.do{ | m | model.changed( m ) };
		}
	}
}

ObjectController : SuperClassOfObjectAndMethodController{
	*new{ | model, spec = ( NonSpec ), msg |
		msg = msg?\objectControllerSync;
		^super.newCopyArgs(model
			, msg
			, spec.asSpec
			, IdentityDictionary( )
			, IdentityDictionary( )
		)
	}

	value{
		^model;
	}

	value_{ | val |
		this.shouldNotImplement( thisMethod );
	}

	input_ { | val |
		this.shouldNotImplement( thisMethod );
	}

	link{ | obj, objset = \value_, spec = ( NonSpec ) |
		this.linkInput( obj, objset, spec.asSpec );
	}

	linkInput{ | obj, objset = \value_, spec = ( NonSpec ) |
		this.linkInputTo( obj, objset, spec.asSpec );
		this.updateLinks;
	}

	linkValue{ | obj, objset = \value_, spec = ( NonSpec ) |
		this.linkValueTo( obj, objset, spec.asSpec );
		this.updateLinks;
	}

	updateLinks {
		msg.do{ | m | model.changed( m ) };
	}
}