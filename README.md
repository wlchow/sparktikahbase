# Apache Spark, Apache Tika and HBase

Spark2 On HBase is not Supported
https://www.cloudera.com/documentation/spark2/latest/topics/spark2_known_issues.html#ki_spark_on_hbase

The hbase-spark module is currently not supported with Spark 2.2. You'll get compile errors.
Need to use Spark 1.6

Tried this with CDH 5.11.1 and 5.13.0

### Prerequisites

Create the HBase table
Note: If you want to use Lily HBase NRT Indexer Service then for every existing table, set the REPLICATION_SCOPE on every column family that you want to index:
https://www.cloudera.com/documentation/enterprise/latest/topics/search_use_hbase_indexer_service.html

```
hbase(main):002:0> whoami
william@HADOOP.WCHOW.PVE (auth:KERBEROS)
    groups: domain, users, spark_users, cdh_admins, sentry_admins

hbase(main):003:0> create 'tbl1', {NAME => 'cf1' , REPLICATION_SCOPE => 1}, {NAME => 'cf2', REPLICATION_SCOPE => 1}
0 row(s) in 3.2330 seconds

=> Hbase::Table - tbl1
```

### Run it like this
```
spark-submit --class com.cloudera.se.wchow.sparktikahbase.SparkTikaHBase --master yarn --deploy-mode client target/sparktikahbase-1.0-SNAPSHOT.jar /user/william/images tbl1 cf1
```

### Sample output
hbase shell output
```
hbase(main):010:0> count 'tbl1'
3 row(s) in 0.0600 seconds

hbase(main):011:0> count 'tbl1', { INTERVAL => 1}
Current count: 1, row: hdfs://nameservice1/user/william/images/296-1246658839vCW7.jpg
Current count: 2, row: hdfs://nameservice1/user/william/images/african-woman-331287912508yqXc.jpg
Current count: 3, row: hdfs://nameservice1/user/william/images/family-of-three-871290963799xUk.jpg
3 row(s) in 0.0210 seconds

hbase(main):009:0> get 'tbl1', 'hdfs://nameservice1/user/william/images/296-1246658839vCW7.jpg'
COLUMN                                     CELL
 cf1:color_transform                       timestamp=1511301979535, value=YCbCr
 cf1:component_1                           timestamp=1511301979535, value=Y component: Quantization table 0, Sampling factors 1 horiz/1 vert
 cf1:component_2                           timestamp=1511301979535, value=Cb component: Quantization table 1, Sampling factors 1 horiz/1 vert
 cf1:component_3                           timestamp=1511301979535, value=Cr component: Quantization table 1, Sampling factors 1 horiz/1 vert
 cf1:compression_type                      timestamp=1511301979535, value=Baseline
 cf1:content_type                          timestamp=1511301979535, value=image/jpeg
 cf1:data_precision                        timestamp=1511301979535, value=8 bits
 cf1:dct_encode_version                    timestamp=1511301979535, value=25600
 cf1:file_modified_date                    timestamp=1511301979535, value=Tue Nov 21 22:06:17 +00:00 2017
 cf1:file_name                             timestamp=1511301979535, value=apache-tika-3988805326396186879.tmp
 cf1:file_size                             timestamp=1511301979535, value=222009 bytes
 cf1:flags_0                               timestamp=1511301979535, value=192
 cf1:flags_1                               timestamp=1511301979535, value=0
 cf1:image_height                          timestamp=1511301979535, value=960 pixels
 cf1:image_width                           timestamp=1511301979535, value=1280 pixels
 cf1:number_of_components                  timestamp=1511301979535, value=3
 cf1:quality                               timestamp=1511301979535, value=60
 cf1:resolution_units                      timestamp=1511301979535, value=none
 cf1:thumbnail_height_pixels               timestamp=1511301979535, value=0
 cf1:thumbnail_width_pixels                timestamp=1511301979535, value=0
 cf1:tiff_bitspersample                    timestamp=1511301979535, value=8
 cf1:tiff_imagelength                      timestamp=1511301979535, value=960
 cf1:tiff_imagewidth                       timestamp=1511301979535, value=1280
 cf1:x_parsed_by                           timestamp=1511301979535, value=org.apache.tika.parser.DefaultParser
 cf1:x_resolution                          timestamp=1511301979535, value=100 dots
 cf1:y_resolution                          timestamp=1511301979535, value=100 dots
26 row(s) in 0.0200 seconds

hbase(main):003:0> get 'tbl1', 'hdfs://nameservice1/user/william/images/african-woman-331287912508yqXc.jpg'
COLUMN                                     CELL
 cf1:application_record_version            timestamp=1511301979535, value=2
 cf1:caption_digest                        timestamp=1511301979535, value=252 225 31 137 200 183 201 120 47 52 98 52 7 88 119 235
 cf1:coded_character_set                   timestamp=1511301979535, value=UTF-8
 cf1:color_transform                       timestamp=1511301979535, value=YCbCr
 cf1:component_1                           timestamp=1511301979535, value=Y component: Quantization table 0, Sampling factors 1 horiz/1 vert
 cf1:component_2                           timestamp=1511301979535, value=Cb component: Quantization table 1, Sampling factors 1 horiz/1 vert
 cf1:component_3                           timestamp=1511301979535, value=Cr component: Quantization table 1, Sampling factors 1 horiz/1 vert
 cf1:compression_type                      timestamp=1511301979535, value=Baseline
 cf1:content_type                          timestamp=1511301979535, value=image/jpeg
 cf1:creator_tool                          timestamp=1511301979535, value=PaintShop Photo Pro 13.00
 cf1:data_precision                        timestamp=1511301979535, value=8 bits
 cf1:dct_encode_version                    timestamp=1511301979535, value=25600
 cf1:file_modified_date                    timestamp=1511301979535, value=Tue Nov 21 22:06:18 +00:00 2017
 cf1:file_name                             timestamp=1511301979535, value=apache-tika-8086146079684179692.tmp
 cf1:file_size                             timestamp=1511301979535, value=178488 bytes
 cf1:flags_0                               timestamp=1511301979535, value=192
 cf1:flags_1                               timestamp=1511301979535, value=0
 cf1:image_height                          timestamp=1511301979535, value=853 pixels
 cf1:image_width                           timestamp=1511301979535, value=1280 pixels
 cf1:number_of_components                  timestamp=1511301979535, value=3
 cf1:quality                               timestamp=1511301979535, value=60
 cf1:tiff_bitspersample                    timestamp=1511301979535, value=8
 cf1:tiff_imagelength                      timestamp=1511301979535, value=853
 cf1:tiff_imagewidth                       timestamp=1511301979535, value=1280
 cf1:x_parsed_by                           timestamp=1511301979535, value=org.apache.tika.parser.DefaultParser
 cf1:xmp_value_count                       timestamp=1511301979535, value=6
 cf1:xmpmm_documentid                      timestamp=1511301979535, value=xmp.did:25ADD70DDF6311DF9EFED45649EEA978
27 row(s) in 0.0420 seconds

hbase(main):005:0> get 'tbl1', 'hdfs://nameservice1/user/william/images/family-of-three-871290963799xUk.jpg'
COLUMN                                     CELL
 cf1:application_record_version            timestamp=1511301979535, value=2
 cf1:author                                timestamp=1511301979535, value=Petr Kratochvil
 cf1:blue_colorant                         timestamp=1511301979535, value=(0.1431, 0.0606, 0.7141)
 cf1:blue_trc                              timestamp=1511301979535, value=0.0, 0.0000763, 0.0001526, 0.0002289, 0.0003052, 0.0003815, 0.0004578, 0.0005341, 0.0006104,
                                            0.0006867, 0.000763, 0.0008392, 0.0009003, 0.0009766, 0.0010529, 0.0011292, 0.0012055, 0.0012818, 0.0013581, 0.0014343, 0.
                                           0015106, 0.0015869, 0.0016632, 0.0017395, 0.0018158, 0.0018921, 0.0019684, 0.0020447, 0.002121, 0.0021973, 0.0022736, 0.002
                                           3499, 0.0024262, 0.0025025, 0.0025788, 0.0026551, 0.0027161, 0.0027924, 0.0028687, 0.002945, 0.0030213, 0.0030976, 0.003173
                                           9, 0.0032502, 0.0033417, 0.003418, 0.0
 cf1:caption_digest                        timestamp=1511301979535, value=252 225 31 137 200 183 201 120 47 52 98 52 7 88 119 235
 cf1:class                                 timestamp=1511301979535, value=Display Device
 cf1:cmm_type                              timestamp=1511301979535, value=Lino
 cf1:coded_character_set                   timestamp=1511301979535, value=UTF-8
 cf1:color_space                           timestamp=1511301979535, value=RGB
 cf1:color_transform                       timestamp=1511301979535, value=YCbCr
 cf1:component_1                           timestamp=1511301979535, value=Y component: Quantization table 0, Sampling factors 1 horiz/1 vert
 cf1:component_2                           timestamp=1511301979535, value=Cb component: Quantization table 1, Sampling factors 1 horiz/1 vert
 cf1:component_3                           timestamp=1511301979535, value=Cr component: Quantization table 1, Sampling factors 1 horiz/1 vert
 cf1:compression_type                      timestamp=1511301979535, value=Baseline
 cf1:content_type                          timestamp=1511301979535, value=image/jpeg
 cf1:copyright                             timestamp=1511301979535, value=Copyright (c) 1998 Hewlett-Packard Company
 cf1:creator                               timestamp=1511301979535, value=Petr Kratochvil
 cf1:creator_tool                          timestamp=1511301979535, value=Adobe Photoshop CS5 Windows
 cf1:data_precision                        timestamp=1511301979535, value=8 bits
 cf1:dc_creator                            timestamp=1511301979535, value=Petr Kratochvil
 cf1:dc_title                              timestamp=1511301979535, value=family of three
 cf1:dct_encode_version                    timestamp=1511301979535, value=25600
 cf1:device_manufacturer                   timestamp=1511301979535, value=IEC
 cf1:device_mfg_description                timestamp=1511301979535, value=IEC http://www.iec.ch
 cf1:device_model                          timestamp=1511301979535, value=sRGB
 cf1:device_model_description              timestamp=1511301979535, value=IEC 61966-2.1 Default RGB colour space - sRGB
 cf1:file_modified_date                    timestamp=1511301979535, value=Tue Nov 21 22:06:18 +00:00 2017
 cf1:file_name                             timestamp=1511301979535, value=apache-tika-5072324266241378982.tmp
 cf1:file_size                             timestamp=1511301979535, value=262015 bytes
 cf1:flags_0                               timestamp=1511301979535, value=192
 cf1:flags_1                               timestamp=1511301979535, value=0
 cf1:green_colorant                        timestamp=1511301979535, value=(0.3851, 0.7169, 0.0971)
 cf1:green_trc                             timestamp=1511301979535, value=0.0, 0.0000763, 0.0001526, 0.0002289, 0.0003052, 0.0003815, 0.0004578, 0.0005341, 0.0006104,
                                            0.0006867, 0.000763, 0.0008392, 0.0009003, 0.0009766, 0.0010529, 0.0011292, 0.0012055, 0.0012818, 0.0013581, 0.0014343, 0.
                                           0015106, 0.0015869, 0.0016632, 0.0017395, 0.0018158, 0.0018921, 0.0019684, 0.0020447, 0.002121, 0.0021973, 0.0022736, 0.002
                                           3499, 0.0024262, 0.0025025, 0.0025788, 0.0026551, 0.0027161, 0.0027924, 0.0028687, 0.002945, 0.0030213, 0.0030976, 0.003173
                                           9, 0.0032502, 0.0033417, 0.003418, 0.0
 cf1:image_height                          timestamp=1511301979535, value=853 pixels
 cf1:image_width                           timestamp=1511301979535, value=1280 pixels
 cf1:luminance                             timestamp=1511301979535, value=(76.0365, 80, 87.1246)
 cf1:measurement                           timestamp=1511301979535, value=1931 2\xC2\xB0 Observer, Backing (0, 0, 0), Geometry Unknown, Flare 1%, Illuminant D65
 cf1:media_black_point                     timestamp=1511301979535, value=(0, 0, 0)
 cf1:media_white_point                     timestamp=1511301979535, value=(0.9505, 1, 1.0891)
 cf1:meta_author                           timestamp=1511301979535, value=Petr Kratochvil
 cf1:number_of_components                  timestamp=1511301979535, value=3
 cf1:primary_platform                      timestamp=1511301979535, value=Microsoft Corporation
 cf1:profile_connection_space              timestamp=1511301979535, value=XYZ
 cf1:profile_date_time                     timestamp=1511301979535, value=1998:02:09 06:49:00
 cf1:profile_description                   timestamp=1511301979535, value=sRGB IEC61966-2.1
 cf1:profile_size                          timestamp=1511301979535, value=3144
 cf1:quality                               timestamp=1511301979535, value=70
 cf1:red_colorant                          timestamp=1511301979535, value=(0.4361, 0.2225, 0.0139)
 cf1:red_trc                               timestamp=1511301979535, value=0.0, 0.0000763, 0.0001526, 0.0002289, 0.0003052, 0.0003815, 0.0004578, 0.0005341, 0.0006104,
                                            0.0006867, 0.000763, 0.0008392, 0.0009003, 0.0009766, 0.0010529, 0.0011292, 0.0012055, 0.0012818, 0.0013581, 0.0014343, 0.
                                           0015106, 0.0015869, 0.0016632, 0.0017395, 0.0018158, 0.0018921, 0.0019684, 0.0020447, 0.002121, 0.0021973, 0.0022736, 0.002
                                           3499, 0.0024262, 0.0025025, 0.0025788, 0.0026551, 0.0027161, 0.0027924, 0.0028687, 0.002945, 0.0030213, 0.0030976, 0.003173
                                           9, 0.0032502, 0.0033417, 0.003418, 0.0
 cf1:signature                             timestamp=1511301979535, value=acsp
 cf1:tag_count                             timestamp=1511301979535, value=17
 cf1:technology                            timestamp=1511301979535, value=CRT
 cf1:tiff_bitspersample                    timestamp=1511301979535, value=8
 cf1:tiff_imagelength                      timestamp=1511301979535, value=853
 cf1:tiff_imagewidth                       timestamp=1511301979535, value=1280
 cf1:title                                 timestamp=1511301979535, value=family of three
 cf1:viewing_conditions                    timestamp=1511301979535, value=view (0x76696577): 36 bytes
 cf1:viewing_conditions_description        timestamp=1511301979535, value=Reference Viewing Condition in IEC61966-2.1
 cf1:x_parsed_by                           timestamp=1511301979535, value=org.apache.tika.parser.DefaultParser
 cf1:xmp_value_count                       timestamp=1511301979535, value=13
 cf1:xmpmm_documentid                      timestamp=1511301979535, value=xmp.did:C2F2D710FB1111DF85A4F19955A3AD82
 cf1:xyz_values                            timestamp=1511301979535, value=0.964 1 0.825
62 row(s) in 0.0620 seconds
```

