package com.wakaba.config

import org.apache.ibatis.type.BaseTypeHandler
import org.apache.ibatis.type.JdbcType
import java.sql.CallableStatement
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.UUID

class UUIDTypeHandler : BaseTypeHandler<UUID>() {
    override fun setNonNullParameter(ps: PreparedStatement, i: Int, parameter: UUID, jdbcType: JdbcType?) {
        ps.setObject(i, parameter)
    }

    override fun getNullableResult(rs: ResultSet, columnName: String): UUID? =
        rs.getObject(columnName)?.let { UUID.fromString(it.toString()) }

    override fun getNullableResult(rs: ResultSet, columnIndex: Int): UUID? =
        rs.getObject(columnIndex)?.let { UUID.fromString(it.toString()) }

    override fun getNullableResult(cs: CallableStatement, columnIndex: Int): UUID? =
        cs.getObject(columnIndex)?.let { UUID.fromString(it.toString()) }
}
