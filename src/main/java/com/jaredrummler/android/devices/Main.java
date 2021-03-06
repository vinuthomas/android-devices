/*
 * Copyright (C) 2015. Jared Rummler <me@jaredrummler.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.jaredrummler.android.devices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Main {

  // Excel document created from Google's PDF here:
  // https://support.google.com/googleplay/answer/1727131
  // https://storage.googleapis.com/support-kms-prod/F8E95910876F5BC6A1478469B983847FD45A
  // Last updated: 9/23/2015
  private static final URL DEVICES_XLS = Main.class.getClassLoader().getResource("devices.xls");

  private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

  private static final Gson GSON = new Gson();

  private static final File OUTPUT_DIR = new File("json");

  private static final String[] POPULAR_DEVICES = {
      // Acer
      "Iconia Tab 7",
      "Iconia Tab 8",
      "Iconia Tab 10",
      "Liquid",
      "Liquid E3",
      "Liquid Jade",
      "Liquid Jade S",
      "Liquid S3",
      "Liquid Z5",

      // DELL
      "Venue 7",
      "Venue 8",

      // Asus
      "Nexus 7 (2012)",
      "Nexus 7 (2013)",
      "ZenFone 2",
      "ZenFone 4",
      "ZenFone 5",
      "Fonepad 7",
      "MeMO Pad 7",

      // HTC
      "Nexus 9",
      "HTC One",
      "HTC One S",
      "HTC One (E8)",
      "HTC One (M8)",
      "HTC One M9",

      // LGE
      "Nexus 5",
      "LG G Flex",
      "LG G Flex2",
      "LG G2",
      "LG G3",
      "LG G4",
      "LG Optimus 3D",
      "LG Optimus G",
      "LG Optimus G Pro",
      "LG Optimus L9",
      "Optimus 2X",
      "Optimus 3D",
      "Optimus 3D MAX",
      "Optimus Black",
      "Optimus L5",
      "Optimus LTE",
      "Optimus One",
      "Optimus Pad",

      // MOTOROLA
      "Nexus 6",
      "DROID Turbo",
      "MOTO E",
      "MOTO G",
      "MOTO X",
      "Moto X Style",
      "XOOM",

      // OnePlus
      "OnePlus One",

      // SAMSUNG
      "Galaxy A3",
      "Galaxy A5",
      "Galaxy A8",
      "Galaxy Ace Duos",
      "Galaxy Ace Plus",
      "Galaxy Ace Style",
      "Galaxy Core Prime",
      "Galaxy Core2",
      "Galaxy Alpha",
      "Galaxy Ace 4",
      "Galaxy Ace4",
      "Galaxy Grand Prime",
      "Galaxy Note 10.1",
      "Galaxy Note Edge",
      "Galaxy Note Pro 12.2",
      "Galaxy Tab 10.1",
      "Galaxy Tab 7.0 Plus",
      "Galaxy E5",
      "Galaxy E7",
      "Galaxy Fame",
      "Galaxy Grand2",
      "Galaxy Grand Neo",
      "Galaxy S Duos",
      "Galaxy S Duos2",
      "Galaxy S Duos3",
      "Galaxy S3 Neo",
      "Galaxy Nexus",
      "Galaxy Note2",
      "Galaxy Note3",
      "Galaxy Note4",
      "Galaxy Note5",
      "Galaxy Note3 Neo",
      "Galaxy Y",
      "Galaxy S2",
      "Galaxy S3",
      "Galaxy S3 Mini",
      "Galaxy S4",
      "Galaxy S5",
      "Galaxy S6",
      "Galaxy S6 Edge",
      "Galaxy S6 Edge+",
      "Nexus 10",

      // SONY
      "Xperia E3",
      "Xperia S",
      "Xperia T2 Ultra",
      "Xperia Tablet S",
      "Xperia Tablet Z",
      "Xperia M5",
      "Xperia M5 Dual",
      "Xperia Z2",
      "Xperia Z3",
      "Xperia Z4",
      "Xperia Z5 Compact"
  };

  static {
    if (!OUTPUT_DIR.exists()) {
      OUTPUT_DIR.mkdirs();
    }
  }

  public static void main(String[] args) throws IOException {
    List<String[]> devices = getDeviceList(DEVICES_XLS.getPath());
    FileUtils.write(new File(OUTPUT_DIR, "android-devices.json"), PRETTY_GSON.toJson(devices));
    FileUtils.write(new File(OUTPUT_DIR, "android-devices-min.json"), GSON.toJson(devices));
    createDeviceJsonFiles(devices);
    createManufacturerJsonFiles(devices);
    createPopularDevicesJsonFile(devices);
    writeJavaSwitchStatement(devices);
  }

  private static List<String[]> getDeviceList(String xls) throws IOException {
    List<String[]> devices = new ArrayList<>();
    FileInputStream fis = new FileInputStream(new File(xls));
    HSSFWorkbook workbook = new HSSFWorkbook(fis);
    HSSFSheet sheet = workbook.getSheetAt(0);
    boolean firstRow = true;
    for (Iterator<Row> it = sheet.iterator(); it.hasNext(); ) {
      Row row = it.next();
      if (firstRow) {
        firstRow = false;
        continue; // skip header row
      }
      devices.add(new String[]{
          getStringCellValue(row.getCell(0)), // RETAIL NAME
          getStringCellValue(row.getCell(1)), // MARKET NAME
          getStringCellValue(row.getCell(2)), // DEVICE NAME
          getStringCellValue(row.getCell(3))  // MODEL NAME
      });
    }
    fis.close();
    return devices;
  }

  private static String getStringCellValue(Cell cell) {
    switch (cell.getCellType()) {
      case Cell.CELL_TYPE_BOOLEAN:
        return cell.getBooleanCellValue() ? "true" : "false";
      case Cell.CELL_TYPE_NUMERIC:
        return Double.toString(cell.getNumericCellValue());
      case Cell.CELL_TYPE_STRING:
      default:
        return cell.getStringCellValue();
    }
  }

  private static void createDeviceJsonFiles(List<String[]> devices) throws IOException {
    File baseDir = new File(OUTPUT_DIR, "devices");
    if (baseDir.exists()) {
      FileUtils.deleteDirectory(baseDir);
    }
    baseDir.mkdirs();

    // group all devices with the same codename together
    HashMap<String, List<DeviceInfo>> map = new HashMap<>();
    for (String[] arr : devices) {
      String key = arr[2];
      List<DeviceInfo> list = map.get(key);
      if (list == null) {
        list = new ArrayList<>();
      }
      list.add(new DeviceInfo(arr[0], arr[1], arr[2], arr[3]));
      map.put(key, list);
    }

    for (Map.Entry<String, List<DeviceInfo>> entry : map.entrySet()) {
      File file = new File(baseDir, entry.getKey() + ".json");
      FileUtils.write(file, PRETTY_GSON.toJson(entry.getValue()));
    }
  }

  private static void createManufacturerJsonFiles(List<String[]> devices) throws IOException {
    File baseDir = new File(OUTPUT_DIR, "manufacturer");
    if (baseDir.exists()) {
      FileUtils.deleteDirectory(baseDir);
    }
    baseDir.mkdirs();

    // group all devices with the same codename together
    HashMap<String, List<DeviceInfo>> map = new HashMap<>();
    for (String[] arr : devices) {
      String key = arr[0];
      if (key == null || key.trim().length() == 0) {
        continue;
      }
      List<DeviceInfo> list = map.get(key);
      if (list == null) {
        list = new ArrayList<>();
      }
      list.add(new DeviceInfo(null, arr[1], arr[2], arr[3]));
      map.put(key, list);
    }

    for (Map.Entry<String, List<DeviceInfo>> entry : map.entrySet()) {
      String key = entry.getKey();
      File file = new File(baseDir, key + ".json");
      Manufacturer manufacturer = new Manufacturer(key, entry.getValue());
      FileUtils.write(file, PRETTY_GSON.toJson(manufacturer));
    }
  }

  private static void createPopularDevicesJsonFile(List<String[]> devices) throws IOException {
    List<DeviceInfo> deviceInfos = new ArrayList<>();
    List<String[]> commonDevices = new ArrayList<>();
    for (String name : POPULAR_DEVICES) {
      devices.stream().filter(arr -> arr[1].equals(name)).forEach(arr -> {
        deviceInfos.add(new DeviceInfo(arr[0], arr[1], arr[2], arr[3]));
        commonDevices.add(arr);
      });
    }
    FileUtils.write(new File(OUTPUT_DIR, "popular-devices.json"), PRETTY_GSON.toJson(deviceInfos));
    FileUtils.write(new File(OUTPUT_DIR, "popular-devices-min.json"), GSON.toJson(deviceInfos));
    FileUtils.write(new File(OUTPUT_DIR, "common-devices.json"), PRETTY_GSON.toJson(commonDevices));
    FileUtils.write(new File(OUTPUT_DIR, "common-devices-min.json"), GSON.toJson(commonDevices));
  }

  private static void writeJavaSwitchStatement(List<String[]> devices) throws IOException {
    StringBuilder sb = new StringBuilder();
    Map<String, Set<String>> deviceMap = new TreeMap<>((o1, o2) -> {
      return o1.compareToIgnoreCase(o2);
    });

    for (String name : POPULAR_DEVICES) {
      List<String> list = new ArrayList<>();
      Set<String> codenames = new HashSet<>();
      devices.stream().filter(arr -> arr[1].equals(name)).forEach(arr -> {
        list.add(arr[2]);
      });
      Collections.sort(list);
      codenames.addAll(list);
      deviceMap.put(name, codenames);
    }

    // TODO: Use JavaPoet and create a working class.
    sb.append("public static String getDeviceName(String codename, String fallback) {\n");
    sb.append("  switch (codename) {\n");
    for (Map.Entry<String, Set<String>> entry : deviceMap.entrySet()) {
      Set<String> codenames = entry.getValue();
      for (String codename : codenames) {
        sb.append("    case \"" + codename + "\":\n");
      }
      sb.append("      return \"" + entry.getKey() + "\";\n");
    }
    sb.append("    default:\n");
    sb.append("      return fallback;\n\t}\n}");

    System.out.println(sb.toString());
    new File("json").mkdirs();
    FileUtils.write(new File("json/gist.txt"), sb.toString());
  }

  static class Manufacturer {

    public final String manufacturer;

    public final List<DeviceInfo> devices;

    public Manufacturer(String manufacturer, List<DeviceInfo> devices) {
      this.manufacturer = manufacturer;
      this.devices = devices;
    }
  }

  static class DeviceInfo {

    public final String manufacturer;

    public final String market_name;

    public final String codename;

    public final String model;

    public DeviceInfo(String manufacturer, String market_name, String codename, String model) {
      this.manufacturer = manufacturer;
      this.market_name = market_name;
      this.codename = codename;
      this.model = model;
    }
  }
}
