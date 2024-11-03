package com.sellsphere.client.product;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.query.sqm.function.SqmFunctionRegistry;

// Required for ProductSpecifications.hasKeyword to perform full text search
public class MySQLDialectCustom extends MySQLDialect {

  public MySQLDialectCustom() {
    super();
  }

  @Override
  public void initializeFunctionRegistry(FunctionContributions functionContributions) {
    super.initializeFunctionRegistry(functionContributions);

    SqmFunctionRegistry functionRegistry = functionContributions.getFunctionRegistry();

    functionRegistry.registerPattern(
            "match",
            "match(?1, ?2, ?3, ?4) against (?5)"
    );
  }
}