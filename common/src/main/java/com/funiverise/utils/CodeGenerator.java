package com.funiverise.utils;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.po.LikeTable;

import java.util.Collections;

/**
 * @author Funny
 * @version 1.0
 * @description: TODO
 * @date 2021/12/7 14:27
 */
public class CodeGenerator {

    public static void main(String[] args) {
        FastAutoGenerator.create("jdbc:mysql://127.0.0.1:3306/moon","moonlight","1NN7Eax4BeEH4UL")
                .globalConfig(builder -> {
                    builder.author("Funny") // 设置作者
                            .fileOverride() // 覆盖已生成文件
                            .outputDir("D://"); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent("com.funiverise") // 设置父包名
                            .moduleName("gateway") // 设置父包模块名
                            .pathInfo(Collections.singletonMap(OutputFile.mapperXml, "D://xml")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.likeTable(new LikeTable("T_%")).addTablePrefix("T_");
                }).execute();
    }

}
