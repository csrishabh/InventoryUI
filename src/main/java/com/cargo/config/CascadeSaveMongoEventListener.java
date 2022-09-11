package com.cargo.config;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.MappingException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import com.cargo.document.CascadeSave;

@Component
public class CascadeSaveMongoEventListener extends AbstractMongoEventListener<Object> {

	
	@Autowired
    private MongoOperations mongoOperations;
 
	@Override
	  public void onBeforeConvert(BeforeConvertEvent<Object> event) {
		Object source = event.getSource(); 
	      ReflectionUtils.doWithFields(source.getClass(), new ReflectionUtils.FieldCallback() {
	 
	          public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
	              ReflectionUtils.makeAccessible(field);
	 
	              if (field.isAnnotationPresent(DBRef.class) && field.isAnnotationPresent(CascadeSave.class)) {
	                  final Object fieldValue = field.get(source);
	 
	                  FieldCallback callback = new FieldCallback();
	                  if( field.getType().isAssignableFrom(List.class)) {
	                	  ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
	                	  ReflectionUtils.doWithFields((Class<?>) stringListType.getActualTypeArguments()[0], callback);
	                  }
	                  else {
	                  ReflectionUtils.doWithFields(fieldValue.getClass(), callback);
	                  }
	                  if (!callback.isIdFound()) {
	                      throw new MappingException("Cannot perform cascade save on child object without id set");
	                  }
	 	              mongoOperations.insertAll((Collection<? extends Object>) fieldValue);
	              }
	          }
	      });
	}
}
