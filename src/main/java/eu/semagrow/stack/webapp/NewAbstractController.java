/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.semagrow.stack.webapp;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 *
 * @author turnguard
 */
@Controller
@RequestMapping("/tree")
public class NewAbstractController {
    
    public NewAbstractController() {
    }
    
    @RequestMapping("/test")
    public void test(HttpServletResponse response) throws IOException{
        response.getWriter().print("hi semagrowX");
    }
}
