import React from 'react';
import {Overlay, Tooltip, Popover} from 'react-bootstrap';
import {translation} from '../resources/translations'

export default class Login extends React.Component {

  constructor(props){
    super();
    this.id = "LoginForm";
    this.modeChange = props.controller.modeChange;
  }

  render() {
      this.loginError = this.props.error;
      this.errorMsg = "Väärä käyttäjätunnus tai salasana";

      const loginTooltip = (
          <Tooltip id="tooltip">asd</Tooltip>
      );
      const passwordTooltip = (
          <Tooltip id="tooltip"><strong>Holy guacamole!</strong> Check this info.</Tooltip>
      );

      //TODO: move to constructor?
      const loginParams = this.props.loginParams;
      const executionKey = loginParams.executionKey ? loginParams.executionKey : "";
      const loginTicket = loginParams.loginTicket ? loginParams.loginTicket : "";

      return(
          <div className="login-box">
            <form id="credentials"
                  action={"/cas/login?service="+this.props.targetService} method="post">
              <input type="hidden" name="lt" value={loginTicket} />
              <input type="hidden" name="execution" value={executionKey} />
              <input type="hidden" name="_eventId" value="submit" />

              <div>
                  <input id="username" name="username" type="text"
                         ref="login"
                         autoFocus="autoFocus"
                         tabIndex="1"
                         autoComplete="false"
                         className={this.loginError ? "invalid-input error" : "login-input"}
                         placeholder={translation("login.usernamePlaceholder")}
                         onChange={e => this.setState({username: e.target.value})}/>
              </div>
              <div>
                  <input id="password" name="password"
                         type="password"
                         ref="password"
                         tabIndex="2"
                         className={this.loginError ? "invalid-input error" : "login-input"}
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

