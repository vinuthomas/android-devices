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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Main {

  // Excel document created from Google's PDF here:
  // https://support.google.com/googleplay/answer/1727131
  // https://storage.googleapis.com/support-kms-prod/F8E95910876F5BC6A1478469B983847FD45A
  // Last updated: 9/23/2015
  private static final URL DEVICES_XLS = Main.class.getClassLoader().getResource("devices.xls");

  private static final Gson PRETTY_GSON = new GsonBuilder().setPrettyPrinting().create();

  private static final Gson GSON = new Gson();

  private static final File OUTPUT_DIR = new File("json");

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
