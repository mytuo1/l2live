DROP TABLE IF EXISTS `npcbuffer_buff_list`;
CREATE TABLE `npcbuffer_buff_list` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `buff_class` int(2) DEFAULT NULL,
  `buffType` varchar(10) DEFAULT NULL,
  `buffId` int(5) DEFAULT '0',
  `buffLevel` int(5) DEFAULT NULL,
  `forClass` tinyint(1) DEFAULT NULL,
  `canUse` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=145 DEFAULT CHARSET=latin1;

-- ----------------------------
-- Records of npcbuffer_buff_list
-- ----------------------------
INSERT INTO `npcbuffer_buff_list` VALUES ('1', '0', 'buff', '1036', '2', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('2', '0', 'buff', '1040', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('3', '0', 'buff', '1043', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('4', '0', 'buff', '1044', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('5', '0', 'buff', '1045', '6', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('6', '0', 'buff', '1047', '4', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('7', '0', 'buff', '1048', '6', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('8', '0', 'buff', '1059', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('9', '0', 'buff', '1068', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('10', '0', 'buff', '1077', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('11', '0', 'buff', '1085', '3', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('12', '0', 'buff', '1086', '2', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('13', '0', 'buff', '1087', '3', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('14', '0', 'buff', '1204', '2', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('15', '0', 'buff', '1240', '3', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('16', '0', 'buff', '1242', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('17', '0', 'buff', '1243', '6', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('18', '0', 'buff', '1257', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('19', '0', 'buff', '1268', '4', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('20', '0', 'buff', '1303', '2', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('21', '0', 'buff', '1304', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('22', '0', 'buff', '1307', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('23', '0', 'buff', '1311', '6', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('24', '0', 'buff', '1397', '3', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('25', '0', 'buff', '1460', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('26', '0', 'buff', '1232', '3', '3', '0');
INSERT INTO `npcbuffer_buff_list` VALUES ('27', '0', 'buff', '1238', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('28', '0', 'special', '1323', '1', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('29', '0', 'special', '1388', '3', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('30', '0', 'special', '1389', '3', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('31', '1', 'song', '264', '1', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('32', '1', 'song', '265', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('33', '1', 'song', '266', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('34', '1', 'song', '267', '1', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('35', '1', 'song', '268', '1', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('36', '1', 'song', '269', '1', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('37', '1', 'song', '270', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('38', '1', 'song', '304', '1', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('39', '1', 'song', '305', '1', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('40', '1', 'song', '306', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('41', '1', 'song', '308', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('42', '1', 'song', '349', '1', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('43', '1', 'song', '363', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('44', '1', 'song', '364', '1', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('45', '1', 'song', '529', '1', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('46', '1', 'song', '764', '1', '3', '0');
INSERT INTO `npcbuffer_buff_list` VALUES ('47', '1', 'song', '914', '1', '3', '0');
INSERT INTO `npcbuffer_buff_list` VALUES ('48', '2', 'dance', '271', '1', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('49', '2', 'dance', '272', '1', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('50', '2', 'dance', '273', '1', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('51', '2', 'dance', '274', '1', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('52', '2', 'dance', '275', '1', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('53', '2', 'dance', '276', '1', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('54', '2', 'dance', '277', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('55', '2', 'dance', '307', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('56', '2', 'dance', '309', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('57', '2', 'dance', '310', '1', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('58', '2', 'dance', '311', '1', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('59', '2', 'dance', '365', '1', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('60', '2', 'dance', '366', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('61', '2', 'dance', '530', '1', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('64', '3', 'resist', '1461', '1', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('65', '3', 'chant', '1002', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('66', '3', 'chant', '1006', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('67', '3', 'chant', '1007', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('68', '3', 'chant', '1009', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('69', '3', 'chant', '1251', '2', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('70', '3', 'chant', '1252', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('71', '3', 'chant', '1253', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('72', '3', 'chant', '1284', '3', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('73', '3', 'chant', '1308', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('74', '3', 'chant', '1309', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('75', '3', 'chant', '1310', '4', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('76', '3', 'chant', '1362', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('77', '3', 'special', '1499', '1', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('78', '3', 'special', '1500', '1', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('79', '3', 'special', '1501', '1', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('80', '3', 'special', '1502', '1', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('81', '3', 'special', '1503', '1', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('82', '3', 'special', '1504', '1', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('83', '3', 'special', '1519', '1', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('84', '4', 'others', '825', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('85', '4', 'others', '826', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('86', '4', 'others', '827', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('87', '4', 'others', '828', '1', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('88', '4', 'others', '829', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('89', '4', 'others', '830', '1', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('107', '7', 'special', '1062', '2', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('108', '7', 'special', '1355', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('109', '7', 'special', '1356', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('110', '7', 'special', '1357', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('111', '7', 'special', '1363', '1', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('112', '7', 'special', '1413', '1', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('114', '7', 'special', '1457', '1', '3', '0');
INSERT INTO `npcbuffer_buff_list` VALUES ('115', '7', 'special', '4699', '13', '0', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('116', '7', 'special', '4700', '13', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('117', '7', 'special', '4702', '13', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('118', '7', 'special', '4703', '13', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('119', '8', 'resist', '1032', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('120', '8', 'resist', '1033', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('121', '8', 'resist', '1035', '4', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('122', '8', 'resist', '1078', '6', '1', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('123', '8', 'resist', '1182', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('124', '8', 'resist', '1189', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('125', '8', 'resist', '1191', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('126', '8', 'resist', '1259', '4', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('127', '8', 'resist', '1352', '1', '2', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('128', '8', 'resist', '1353', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('129', '8', 'resist', '1354', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('130', '8', 'resist', '1392', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('131', '8', 'resist', '1393', '3', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('132', '2', 'dance', '915', '1', '3', '1');
INSERT INTO `npcbuffer_buff_list` VALUES ('133', '8', 'buff', '1542', '1', '3', '1');