package com.pixeldoctrine.web;

import com.pixeldoctrine.db.BackupResultRepository;
import com.pixeldoctrine.entity.BackupResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Controller
public class StatusController {

    @Autowired
    private BackupResultRepository resultRepository;

    @GetMapping("/status")
    public String greeting(@RequestParam(name="oks", required=false, defaultValue="40") int days, Model model)
            throws SQLException, ParseException {
        List<BackupResult> results = resultRepository.load(days);
        List<String> longDates = new ArrayList<>();
        List<String> shortDates = new ArrayList<>();
        Map<String, Map<String, JobDays>> data = transformToTable(results, longDates, shortDates);
        model.addAttribute("data", data);
        model.addAttribute("longDates", longDates);
        model.addAttribute("shortDates", shortDates);
        return "status";
    }

    private Map<String, Map<String, JobDays>> transformToTable(List<BackupResult> results, List<String> longDates, List<String> shortDates) {
        // gather unique oks
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        List<String> uniqueDays = results.stream()
                .map(r -> dateFormatter.format(r.getDate()))
                .distinct()
                .sorted()
                .collect(toList());
        shortDates.addAll(uniqueDays.stream()
                .map(s -> s.split("-")[2])
                .collect(toList()));
        longDates.addAll(uniqueDays.stream()
                .map(s -> s.endsWith("-01")? s : "")
                .map(s -> s.split("-\\d\\d$")[0])
                .collect(toList()));
        Map<String,Integer> dayToIndex = IntStream.range(0, uniqueDays.size())
                .boxed()
                .collect(toMap(uniqueDays::get, i -> i));
        // services
        List<String> services = results.stream()
                .map(BackupResult::getService)
                .distinct()
                .sorted()
                .collect(toList());
        Map<String,Map<String,JobDays>> data = new LinkedHashMap<>();
        for (String service: services) {
            // jobs in this service
            List<BackupResult> serviceJobs = results.stream()
                    .filter(r -> r.getService().equals(service))
                    .collect(toList());
            Map<String, JobDays> jobDays = serviceJobs.stream()
                    .map(r -> r.getClient()+' '+r.getSystem()+' '+r.getJob())
                    .distinct()
                    .sorted()
                    .collect(LinkedHashMap::new,
                            (map, item) -> map.put(item, new JobDays(uniqueDays)),
                            Map::putAll);
            data.put(service, jobDays);
            for (BackupResult r: serviceJobs) {
                String key = r.getClient() + ' ' + r.getSystem() + ' ' + r.getJob();
                String day = dateFormatter.format(r.getDate());
                jobDays.get(key).setOk(r, dayToIndex.get(day), r.getPercent()==100);
            }
        }
        return data;
    }

    private static class JobDays {
        private boolean[] oks;
        private BackupResult result;
        JobDays(List<String> dates) {
            this.oks = new boolean[dates.size()];
        }
        void setOk(BackupResult r, int index, boolean ok) {
            result = r;
            oks[index] = ok;
        }
        public boolean[] getOks() {
            return oks;
        }
        public BackupResult getResult() {
            return result;
        }
    }
}
