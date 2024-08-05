package com.kpi.mars.palletizeradapter.util;

import com.kpi.mars.palletizeradapter.ClientException;
import com.kpi.roboticshub.api.ApiError;
import com.kpi.roboticshub.api.Response;
import org.springframework.http.HttpStatus;

import javax.management.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kpi.roboticshub.api.ApiErrorConstants.UNEXPECTED_FAILURE_FORMAT;
import static com.kpi.roboticshub.api.ApiErrorConstants.UNEXPECTED_FAILURE_ID;

public class MemoryUtil
{

  private MemoryUtil(){}

  private static String getMemoryUsuage()
  {
    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    StringBuilder stringBuilder = new StringBuilder();
    //1,073,741,824 Bytes (B) = 1 Gigabytes (GB)

    stringBuilder.append("Memory Usage:");

    stringBuilder.append(System.lineSeparator());
    stringBuilder.append(
        "Initial memory: " + (double) memoryMXBean.getHeapMemoryUsage().getInit() / 1073741824 + " GB");
    stringBuilder.append(System.lineSeparator());
    stringBuilder.append(
        "Used heap memory:  " + (double) memoryMXBean.getHeapMemoryUsage().getUsed() / 1073741824 + " GB");
    stringBuilder.append(System.lineSeparator());
    stringBuilder.append(
        "Committed memory: " + (double) memoryMXBean.getHeapMemoryUsage().getCommitted() / 1073741824 + " GB");

    return stringBuilder.toString();
  }

  private static double getProcessCpuLoad()
      throws ReflectionException, InstanceNotFoundException, MalformedObjectNameException
  {
    MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
    AttributeList list = mbs.getAttributes(name, new String[] { "ProcessCpuLoad" });

    if (list.isEmpty())
    {
      return Double.NaN;
    }

    Attribute att = (Attribute) list.get(0);
    Double value = (Double) att.getValue();

    // usually takes a couple of seconds before we get real values
    if (value == -1.0)
    {
      return Double.NaN;
    }
    // returns a percentage value with 1 decimal point precision
    return ((int) (value * 1000) / 10.0);
  }

  private static String getCPUUsage()
  {
    StringBuilder stringBuilder = new StringBuilder();

    stringBuilder.append(System.lineSeparator());
    stringBuilder.append("CPU Usage:");
    try
    {
      stringBuilder.append(String.valueOf(getProcessCpuLoad()) + "%");
    }
    catch (ReflectionException|MalformedObjectNameException|InstanceNotFoundException e)
    {
      ApiError apiError = ApiError.builder().errorId(UNEXPECTED_FAILURE_ID)
          .description(UNEXPECTED_FAILURE_FORMAT.formatted(e.getMessage()))
          .build();
      throw new ClientException("No Instance found", List.of(apiError), HttpStatus.NOT_FOUND);
    }
    return stringBuilder.toString();
  }

  private static String getSpaceDetails()
  {
    StringBuilder stringBuilder = new StringBuilder();
    File cDrive = new File(System.getProperty("user.dir"));
    stringBuilder.append(System.lineSeparator());
    stringBuilder.append("Space Usage: ");
    stringBuilder.append(System.lineSeparator());
    stringBuilder.append("Free space: " + (double) cDrive.getFreeSpace() / 1073741824 + " GB");
    stringBuilder.append(System.lineSeparator());
    return stringBuilder.toString();
  }

  public static Object getMemoryUsage()
  {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(System.lineSeparator());
    stringBuilder.append(getMemoryUsuage());
    stringBuilder.append(getCPUUsage());
    stringBuilder.append(getSpaceDetails());
    return stringBuilder.toString();
  }

  public static Response<Map<Object, Object>> getSystemMemoryUsage()
  {
    Response<Map<Object, Object>> response = new Response<>();
    Map<Object, Object> sysMap = new HashMap<>();
    String currentSystemIP;
    try
    {
      currentSystemIP = "Adapter IP: " + InetAddress.getLocalHost().getHostAddress();
    }
    catch (UnknownHostException e)
    {
      ApiError apiError = ApiError.builder().errorId(UNEXPECTED_FAILURE_ID)
          .description(UNEXPECTED_FAILURE_FORMAT.formatted(e.getMessage()))
          .build();
      throw new ClientException("Unknown Host", List.of(apiError), HttpStatus.NOT_FOUND);
    }

    sysMap.put(currentSystemIP, getMemoryUsage());
    response.setMessage(sysMap);

    return response;
  }
}
