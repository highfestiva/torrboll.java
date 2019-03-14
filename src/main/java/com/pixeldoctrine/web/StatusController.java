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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class StatusController {

    @Autowired
    private BackupResultRepository resultRepository;

    @GetMapping("/status")
    public String greeting(@RequestParam(name="days", required=false, defaultValue="40") int days, Model model)
            throws SQLException, ParseException {
        List<BackupResult> results = resultRepository.load(days);
        Map<String,Map> data = transformToTable(results);
        model.addAttribute("data", data);
        return "status";
    }

    private Map<String,Map> transformToTable(List<BackupResult> results) {
        Map<String,Map> data = new LinkedHashMap<>();
        for (BackupResult result: results) {
            data.computeIfAbsent(result.getService(), k -> new LinkedHashMap<>());
            //Map<String,Object> serviceMap = data.get(result.getService());
        }
        /*data.add(new LinkedHashMap<>());
        data.add(new LinkedHashMap<>());
        data.get(0).put("service", "Ahsay");
        data.get(1).put("service", "StorageCraft");*/
        return data;
    }
}
