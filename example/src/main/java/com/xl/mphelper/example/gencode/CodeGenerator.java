package com.xl.mphelper.example.gencode;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

/**
 * @author tanjl11
 * @date 2021/10/27 14:36
 */
public class CodeGenerator {


    public static void main(String[] args) {
        AutoGenerator generator = new AutoGenerator();
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setAuthor("lele");
        gc.setOutputDir(projectPath + "/example/target/code");
        generator.setGlobalConfig(gc);
        dataSourceConfig.setDbType(DbType.MYSQL)
                .setPassword("123456")
                .setUsername("root")
                .setDriverName("com.mysql.jdbc.Driver")
                .setUrl("jdbc:mysql://localhost:3306/demo?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true&useSSL=false&serverTimezone=Asia/Shanghai")
                .setTypeConvert(new MySqlTypeConvert() {
                    @Override
                    public IColumnType processTypeConvert(GlobalConfig config, String fieldType) {
                        if (fieldType.equals("datetime") || fieldType.equals("timestamp")) {
                            return DbColumnType.LOCAL_DATE_TIME;
                        }
                        if (fieldType.equals("decimal")) {
                            return DbColumnType.BIG_DECIMAL;
                        }
                        return super.processTypeConvert(config, fieldType);
                    }
                });
        generator.setDataSource(dataSourceConfig);
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.xl.mphelper.example");
        generator.setPackageInfo(pc);
        StrategyConfig strategy = new StrategyConfig();
        strategy.setEntityLombokModel(true);
        strategy.setRestControllerStyle(true);
        strategy.setEntityTableFieldAnnotationEnable(true);
        strategy.setNaming(NamingStrategy.underline_to_camel);
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        strategy.setSuperMapperClass("com.xl.mphelper.mapper.CustomMapper");
        strategy.setInclude("order_info","order_detail");
        strategy.setSkipView(true);
        generator.setStrategy(strategy);
        generator.execute();
    }
}
