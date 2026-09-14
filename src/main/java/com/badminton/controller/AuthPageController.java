package com.badminton.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AuthPageController {

    @GetMapping("/admin/login")
    @ResponseBody
    public String adminLoginPage(CsrfToken token) {
        return """
            <html><body>
            <h1>Admin Login</h1>
            <form method='post' action='/login'>
              <input type='hidden' name='%s' value='%s'/>
              <input type='hidden' name='loginType' value='admin'/>
              <input name='username' placeholder='username'/>
              <input name='password' type='password' placeholder='password'/>
              <button type='submit'>Login</button>
            </form>
            <a href='/admin/register'>Admin Register</a>
            </body></html>
            """.formatted(token.getParameterName(), token.getToken());
    }

    @GetMapping("/participant/login")
    @ResponseBody
    public String participantLoginPage(CsrfToken token) {
        return """
            <html><body>
            <h1>Participant Login</h1>
            <form method='post' action='/login'>
              <input type='hidden' name='%s' value='%s'/>
              <input type='hidden' name='loginType' value='participant'/>
              <input name='username' placeholder='username'/>
              <input name='password' type='password' placeholder='password'/>
              <button type='submit'>Login</button>
            </form>
            <a href='/participant/register'>Participant Register</a>
            </body></html>
            """.formatted(token.getParameterName(), token.getToken());
    }

    @GetMapping("/participant/register")
    @ResponseBody
    public String participantRegisterPage(CsrfToken token) {
        return """
            <html><body>
            <h1>Participant Register</h1>
            <form method='post' action='/participant/register'>
              <input type='hidden' name='%s' value='%s'/>
              <input name='username' placeholder='username'/>
              <input name='email' type='email' placeholder='email'/>
              <input name='displayName' placeholder='display name'/>
              <input name='password' type='password' placeholder='password'/>
              <button type='submit'>Register</button>
            </form>
            </body></html>
            """.formatted(token.getParameterName(), token.getToken());
    }

    @GetMapping("/admin/register")
    @ResponseBody
    public String adminRegisterPage(CsrfToken token) {
        return """
            <html><body>
            <h1>Admin Register</h1>
            <form method='post' action='/admin/register'>
              <input type='hidden' name='%s' value='%s'/>
              <input name='username' placeholder='username'/>
              <input name='email' type='email' placeholder='email'/>
              <input name='displayName' placeholder='display name'/>
              <input name='password' type='password' placeholder='password'/>
              <button type='submit'>Register</button>
            </form>
            </body></html>
            """.formatted(token.getParameterName(), token.getToken());
    }

    @GetMapping("/admin/dashboard")
    @ResponseBody
    public String adminDashboard() {
        return "admin dashboard";
    }

    @GetMapping("/participant/dashboard")
    @ResponseBody
    public String participantDashboard() {
        return "participant dashboard";
    }
}
