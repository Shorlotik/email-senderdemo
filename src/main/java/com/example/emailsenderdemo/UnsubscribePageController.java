package com.example.emailsenderdemo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class UnsubscribePageController {

    @GetMapping("/unsubscribed")
    public String unsubscribedPage(Model model) {
        model.addAttribute("message", "Вы успешно отписались от рассылки.");
        return "unsubscribed";
    }

    @GetMapping("/unsubscribed-error")
    public String unsubscribedErrorPage(Model model) {
        model.addAttribute("message", "Ошибка: Email не найден или уже отписан.");
        return "unsubscribed";
    }
}
