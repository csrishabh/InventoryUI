package com.cargo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import com.cargo.document.Manifest;
import com.cargo.repo.SequenceGenerator;

@Component
public class ManifestListener extends AbstractMongoEventListener<Manifest> {

		@Autowired
		private SequenceGenerator sequenceGenerator;
		
		@Override
		public void onBeforeConvert(BeforeConvertEvent<Manifest> event) {
		    if (event.getSource().getRefId() == null) {
		        event.getSource().setRefId(String.valueOf(sequenceGenerator.generateSequence(Manifest.SEQUENCE_NAME)));
		    }
		}
		

}
