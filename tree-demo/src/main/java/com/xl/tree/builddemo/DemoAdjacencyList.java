package com.xl.tree.builddemo;

import lombok.Data;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author tanjl11
 * @date 2022/01/12 11:41
 */
@Data
public class DemoAdjacencyList implements AdjacencyList<DemoAdjacencyList> {
    private Long pathDataBaseId;
    private String fullPath;
    private String pidCode;
    private String idCode;
    //位置号
    private String lineNo;

    private Date from;
    private Date to;


    public static DemoAdjacencyList build(String pidCode, String idCode, String lineNo) {
        DemoAdjacencyList res = new DemoAdjacencyList();
        res.setIdCode(idCode);
        res.setPidCode(pidCode);
        res.lineNo = lineNo;
        return res;
    }

    public static void buildAndAdd(String pidCode, String idCode, String lineNo, List<DemoAdjacencyList> dataBase, String from, String to) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        DemoAdjacencyList res = new DemoAdjacencyList();
        res.setIdCode(idCode);
        res.setPidCode(pidCode);
        res.lineNo = lineNo;
        try {
            res.from = format.parse(from);
            res.to = format.parse(to);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dataBase.add(res);
    }


    @Override
    public Long pathDataBaseId() {
        return pathDataBaseId;
    }

    @Override
    public void setPathDataBaseId(Long id) {
        this.pathDataBaseId = id;
    }

    @Override
    public String fullPath() {
        return fullPath;
    }

    @Override
    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    @Override
    public Serializable pid() {
        return this.pidCode;
    }

    @Override
    public Serializable nodeId() {
        return idCode;
    }


    @Override
    public String getUniqueKey() {
        return idCode + pidCode + lineNo;
    }
}
