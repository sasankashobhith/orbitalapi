package com.bundee.msfw.interfaces.dbi;

public interface DBQueryBuilder {

    DBQueryBuilder setBindInputFunction(BindInput bindInputFunction);

    DBQueryBuilder setFetchDataFunction(FetchData fetchDataFunction);

    DBQueryBuilder setQueryString(String queryString);

    DBQueryBuilder setRegisterOutParameterTypes(RegisterOutParameterTypes registerOutParameterTypes);

    DBQueryBuilder setBatch();

    DBQueryBuilder setReturnKeys();
    
    DBQueryBuilder logQuery(boolean bLogQ);

    DBQueryBuilder logTrace(boolean bLogQ);
    
    DBQueryBuilder throwOnNoData(boolean bThrow);
    
    DBQuery build();
}
