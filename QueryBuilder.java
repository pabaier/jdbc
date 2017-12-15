public class QueryBuilder {

    private String columns;
    private String table;
    private String where;
    private String orderBy;
    private boolean distinct;

    public QueryBuilder() {
        columns = "*";
        table = null;
        where = null;
        orderBy = null;
        distinct = false;
    }

    public QueryBuilder setSelectedColumns(String columns) {
        this.columns = columns;
        return this;
    }

    // this must be set
    public QueryBuilder setTable(String tableName) {
        table = tableName;
        return this;
    }

    public QueryBuilder setWhere(String where) {
        this.where = where;
        return this;
    }

    public QueryBuilder setOrderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    public QueryBuilder setDistinct(Boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    // SELECT *columns* FROM *table* WHERE *where* ORDER BY *orderBy* 
    public String build() {
        StringBuilder s = new StringBuilder("Select ");
        if(distinct)
            s.append("DISTINCT ");
        s.append(columns + " ");
        s.append("FROM " + table + " ");
        if(where != null)
            s.append("WHERE " + where + " ");
        if(orderBy != null)
            s.append("ORDER BY " + orderBy);
        
        return s.toString().trim();
    }

}