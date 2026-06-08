package com.rx.admin.service;

import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.*;
import java.util.*;

@Service
public class HealthService {

    public Map<String, Object> getSystemHealth() {
        Map<String, Object> data = new LinkedHashMap<>();

        // CPU
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        Map<String, Object> cpu = new LinkedHashMap<>();
        cpu.put("cores", Runtime.getRuntime().availableProcessors());
        double loadAvg = os.getSystemLoadAverage();
        cpu.put("usage", Math.round(loadAvg / Runtime.getRuntime().availableProcessors() * 1000.0) / 10.0);
        cpu.put("loadAverage", Math.round(loadAvg * 100.0) / 100.0);
        data.put("cpu", cpu);

        // Memory (系统)
        Runtime rt = Runtime.getRuntime();
        long totalMem = rt.totalMemory();
        long freeMem = rt.freeMemory();
        long usedMem = totalMem - freeMem;
        long maxMem = rt.maxMemory();
        Map<String, Object> mem = new LinkedHashMap<>();
        mem.put("total", totalMem / 1024 / 1024);
        mem.put("used", usedMem / 1024 / 1024);
        mem.put("free", freeMem / 1024 / 1024);
        mem.put("max", maxMem / 1024 / 1024);
        mem.put("usage", Math.round((double) usedMem / maxMem * 1000.0) / 10.0);
        mem.put("unit", "MB");
        data.put("memory", mem);

        // JVM
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memBean.getHeapMemoryUsage();
        MemoryUsage nonHeap = memBean.getNonHeapMemoryUsage();
        Map<String, Object> jvm = new LinkedHashMap<>();
        jvm.put("heapUsed", heap.getUsed() / 1024 / 1024);
        jvm.put("heapMax", heap.getMax() / 1024 / 1024);
        jvm.put("nonHeapUsed", nonHeap.getUsed() / 1024 / 1024);
        jvm.put("unit", "MB");
        data.put("jvm", jvm);

        // Threads
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        Map<String, Object> threads = new LinkedHashMap<>();
        threads.put("current", threadBean.getThreadCount());
        threads.put("peak", threadBean.getPeakThreadCount());
        threads.put("daemon", threadBean.getDaemonThreadCount());
        data.put("threads", threads);

        // Disk
        File[] roots = File.listRoots();
        if (roots != null && roots.length > 0) {
            File root = roots[0];
            Map<String, Object> disk = new LinkedHashMap<>();
            disk.put("total", root.getTotalSpace() / 1024 / 1024 / 1024);
            disk.put("free", root.getFreeSpace() / 1024 / 1024 / 1024);
            disk.put("used", (root.getTotalSpace() - root.getFreeSpace()) / 1024 / 1024 / 1024);
            disk.put("usage", Math.round((1.0 - (double) root.getFreeSpace() / root.getTotalSpace()) * 1000.0) / 10.0);
            disk.put("unit", "GB");
            data.put("disk", disk);
        }

        return data;
    }

    public Map<String, Object> getGcStats() {
        Map<String, Object> gc = new LinkedHashMap<>();
        long totalCount = 0, totalTime = 0;
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            totalCount += gcBean.getCollectionCount();
            totalTime += gcBean.getCollectionTime();
        }
        gc.put("totalCount", totalCount);
        gc.put("totalTime", totalTime);
        gc.put("totalTimeSeconds", Math.round(totalTime / 1000.0 * 100.0) / 100.0);
        return gc;
    }
}
