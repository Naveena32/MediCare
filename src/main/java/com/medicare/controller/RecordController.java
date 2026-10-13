package com.medicare.controller;

import com.medicare.dto.MedicalRecordDto;
import com.medicare.entity.MedicalRecord;
import com.medicare.entity.User;
import com.medicare.service.MedicalRecordService;
import com.medicare.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/records")
public class RecordController {

    private final MedicalRecordService recordService;
    private final UserService userService;

    public static final List<String> CATEGORIES = Arrays.asList(
            "Lab Result", "Prescription", "Scan / Imaging",
            "Doctor Note", "Discharge Summary", "Vaccination", "Other"
    );

    public RecordController(MedicalRecordService recordService, UserService userService) {
        this.recordService = recordService;
        this.userService = userService;
    }

    @GetMapping
    public String viewRecords(@RequestParam(value = "q", required = false) String query,
                              @RequestParam(value = "category", required = false) String category,
                              Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        List<MedicalRecord> records = recordService.getRecords(user, query, category);

        model.addAttribute("records", records);
        model.addAttribute("query", query != null ? query : "");
        model.addAttribute("selectedCategory", category != null && !category.isBlank() ? category : "All");
        model.addAttribute("categories", CATEGORIES);
        model.addAttribute("totalCount", records.size());

        return "records/view";
    }

    @GetMapping("/upload")
    public String uploadRecordForm(Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("recordDto", new MedicalRecordDto());
        model.addAttribute("categories", CATEGORIES);
        return "records/upload";
    }

    @PostMapping("/upload")
    public String handleUploadRecord(@Valid @ModelAttribute("recordDto") MedicalRecordDto recordDto,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("errorMessage", "Please check the form inputs and try again.");
            return "records/upload";
        }

        MedicalRecord record = recordService.createRecord(user, recordDto);
        redirectAttributes.addFlashAttribute("successMessage",
                "Medical record \"" + record.getTitle() + "\" added successfully!");
        return "redirect:/records";
    }

    @GetMapping("/{id}")
    public String recordDetail(@PathVariable("id") Long id, Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        MedicalRecord record = recordService.getRecordByIdAndUser(id, user)
                .orElse(null);
        if (record == null) {
            return "redirect:/records";
        }

        model.addAttribute("record", record);
        return "records/detail";
    }

    @GetMapping("/{id}/edit")
    public String editRecordForm(@PathVariable("id") Long id, Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        MedicalRecord record = recordService.getRecordByIdAndUser(id, user).orElse(null);
        if (record == null) {
            return "redirect:/records";
        }

        MedicalRecordDto dto = new MedicalRecordDto();
        dto.setId(record.getId());
        dto.setTitle(record.getTitle());
        dto.setCategory(record.getCategory());
        dto.setDoctorName(record.getDoctorName());
        dto.setRecordDate(record.getRecordDate());
        dto.setNotes(record.getNotes());
        dto.setExistingFileName(record.getFileName());
        dto.setExistingFilePath(record.getFilePath());

        model.addAttribute("recordDto", dto);
        model.addAttribute("record", record);
        model.addAttribute("categories", CATEGORIES);
        return "records/edit";
    }

    @PostMapping("/{id}/edit")
    public String handleEditRecord(@PathVariable("id") Long id,
                                   @Valid @ModelAttribute("recordDto") MedicalRecordDto recordDto,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        MedicalRecord record = recordService.getRecordByIdAndUser(id, user).orElse(null);
        if (record == null) {
            return "redirect:/records";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("record", record);
            model.addAttribute("categories", CATEGORIES);
            model.addAttribute("errorMessage", "Please correct the errors below.");
            return "records/edit";
        }

        recordService.updateRecord(record, recordDto);
        redirectAttributes.addFlashAttribute("successMessage",
                "Record \"" + record.getTitle() + "\" updated successfully!");
        return "redirect:/records/" + id;
    }

    @GetMapping("/{id}/delete")
    public String confirmDeleteRecord(@PathVariable("id") Long id, Model model) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        MedicalRecord record = recordService.getRecordByIdAndUser(id, user).orElse(null);
        if (record == null) {
            return "redirect:/records";
        }

        model.addAttribute("record", record);
        return "records/confirm-delete";
    }

    @PostMapping("/{id}/delete")
    public String handleDeleteRecord(@PathVariable("id") Long id,
                                     RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser();
        if (user == null) {
            return "redirect:/login";
        }

        MedicalRecord record = recordService.getRecordByIdAndUser(id, user).orElse(null);
        if (record != null) {
            String title = record.getTitle();
            recordService.deleteRecord(record);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Record \"" + title + "\" has been deleted.");
        }

        return "redirect:/records";
    }
}
