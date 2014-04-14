package eu.semagrow.stack.webapp.controllers;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author http://www.turnguard.com/turnguard
 */
@Controller
@RequestMapping("/")
public class IndexController {
    
    @RequestMapping(value="/welcome", method=RequestMethod.GET)
    public ModelAndView welcome(HttpServletResponse response) throws IOException{
        ModelAndView mav = new ModelAndView("welcome");
        return mav;
    }
}
