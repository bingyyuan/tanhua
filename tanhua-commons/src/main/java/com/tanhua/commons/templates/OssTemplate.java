package com.tanhua.commons.templates;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.tanhua.commons.properties.OssProperties;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class OssTemplate {

    private OssProperties ossProperties;

    public OssTemplate(OssProperties ossProperties){
        this.ossProperties = ossProperties;
    }

    /**
     * 上传文件
     * @param filename
     * @param is
     * @return
     */
    public String upload(String filename, InputStream is) {
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = ossProperties.getEndpoint();
        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
        String accessKeyId = ossProperties.getAccessKeyId();
        String accessKeySecret = ossProperties.getAccessKeySecret();

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 防止 文件名重复，分文件夹的方式来存储
        String suffix = filename.substring(filename.lastIndexOf("."));
        String ymd = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
        String name = "images/" + ymd + "/" + UUID.randomUUID().toString() + suffix;
        // images/2021/03/06/文件名.jpg

        // 上传文件流。
        ossClient.putObject(ossProperties.getBucketName(), name, is);

        // 关闭OSSClient。
        ossClient.shutdown();
        return ossProperties.getUrl() + "/" + name;
    }

    /**
     *
     * @param objectName 数据库查询到avatar的值, 全路径
     */
    public void deleteFile(String objectName) {
        // Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = ossProperties.getEndpoint();
        // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = ossProperties.getAccessKeyId();
        String accessKeySecret = ossProperties.getAccessKeySecret();
        String bucketName = ossProperties.getBucketName();
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // 删除文件。如需删除文件夹，请将ObjectName设置为对应的文件夹名称。如果文件夹非空，则需要将文件夹下的所有object删除后才能删除该文件夹。
        ossClient.deleteObject(bucketName, objectName.replace(ossProperties.getUrl()+ "/",""));
        // 关闭OSSClient。
        ossClient.shutdown();
        // url: https://sztanhua.oss-cn-shenzhen.aliyuncs.com/ 替换成""
        //https://sztanhua.oss-cn-shenzhen.aliyuncs.com/images/2021/03/06/6c4991af-a2a5-490b-bcac-c9495651d79f.jpg
        // images/2021/03/06/6c4991af-a2a5-490b-bcac-c9495651d79f.jpg 就可删除
    }


}