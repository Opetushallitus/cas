import React from 'react';
import {translation} from '../translations'

const Login = ({modeChange, configuration, targetService, loginError}) => {

  const executionKey = configuration.executionKey ? configuration.executionKey : "";
  const loginTicket = configuration.loginTicket ? configuration.loginTicket : "";

  return(
      <div className="login-box">
        <form id="credentials"
              action={"/cas/login?service="+targetService} method="post">
          <input type="hidden" name="lt" value={loginTicket} />
          <input type="hidden" name="execution" value={executionKey} />
          <input type="hidden" name="_eventId" value="submit" />

          <div>
              <input id="username" name="username" type="text"
                     autoFocus="autoFocus"
                     tabIndex="1"
                     autoComplete="false"
                     className={loginError ? "invalid-input error" : "login-input"}
                     placeholder={translation("login.usernamePlaceholder")}/>
          </div>
          <div>
              <input id="password" name="password"
                     type="password"
                     tabIndex="2"
                     className={loginError ? "invalid-input error" : "login-input"}
                     placeholder={translation("login.passwordPlaceholder")}/>
          </div>
          <div className="link" onClick={modeChange}>
              {translation("login.forgotPassword")}
          </div>
          <input type="submit" className="btn btn-login" value={translation("login.button")}/>
         </form>
        <div className={loginError ? "errortext" : ""}>{loginError ? translation("login.error."+loginError) : ""}</div>
      </div>
  )
};

export default Login;


