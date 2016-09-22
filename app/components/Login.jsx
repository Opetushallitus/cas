import React from 'react';
import {Overlay, Tooltip, Popover} from 'react-bootstrap';
import {translation} from '../translations'

export default class Login extends React.Component {

  constructor(props){
    super();

    this.modeChange = props.controller.modeChange;

    const configuration = props.configuration;
    this.executionKey = configuration.executionKey ? configuration.executionKey : "";
    this.loginTicket = configuration.loginTicket ? configuration.loginTicket : "";
  }

  render() {
      const loginError = this.props.error;
      const errorMsg =  loginError ? translation("login.error."+loginError) : "";
      console.log("Login error: "+ errorMsg);
      const loginTooltip = (
          <Tooltip id="tooltip">asd</Tooltip>
      );
      const passwordTooltip = (
          <Tooltip id="tooltip"><strong>Holy guacamole!</strong> Check this info.</Tooltip>
      );

      return(
          <div className="login-box">
            <form id="credentials"
                  action={"/cas/login?service="+this.props.targetService} method="post">
              <input type="hidden" name="lt" value={this.loginTicket} />
              <input type="hidden" name="execution" value={this.executionKey} />
              <input type="hidden" name="_eventId" value="submit" />

              <div>
                  <input id="username" name="username" type="text"
                         ref="login"
                         autoFocus="autoFocus"
                         tabIndex="1"
                         autoComplete="false"
                         className={loginError ? "invalid-input error" : "login-input"}
                         placeholder={translation("login.usernamePlaceholder")}
                         onChange={e => this.setState({username: e.target.value})}/>
              </div>
              <div>
                  <input id="password" name="password"
                         type="password"
                         ref="password"
                         tabIndex="2"
                         className={loginError ? "invalid-input error" : "login-input"}
                         placeholder={translation("login.passwordPlaceholder")}
                         onChange={e => this.setState({password: e.target.value})}/>
              </div>
              <div ref="pwreset" className="link" onClick={this.modeChange}>
                  {translation("login.forgotPassword")}
              </div>
              <input type="submit" className="btn btn-login" value={translation("login.button")}/>
             </form>
          </div>
      )
  }
}

