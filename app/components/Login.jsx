import React from 'react';
import {Overlay, Tooltip, Popover} from 'react-bootstrap';
import {translation} from '../resources/translations'

export default class Login extends React.Component {

  constructor(props){
    super();
    this.id = "LoginForm";
    this.modeChange = props.controller.modeChange;


    this.state = {username: '',
                  password: ''}
  }

  componentDidUpdate(){
    if(this.props.submitForm){
      this.loginForm.submit();
    }
  }

  render() {

      this.loginError = this.props.error && this.props.error.type === "login";
      this.errorMsg = this.props.error ? this.props.error.message : '';

      const controller = this.props.controller;
      const loginTooltip = (
          <Tooltip id="tooltip">asd</Tooltip>
      );
      const passwordTooltip = (
          <Tooltip id="tooltip"><strong>Holy guacamole!</strong> Check this info.</Tooltip>
      );

      const loginParams = this.props.loginParams;
      var executionKey = loginParams.executionKey ? loginParams.executionKey : "";
      var loginTicket = loginParams.loginTicket ? loginParams.loginTicket : "";

      return(
          <div className="login-box">
            <form ref={(ref) => this.loginForm = ref}
                  id="credentials"
                  action={"/cas/login?service="+this.props.targetService} method="post">
              <input type="hidden" name="lt" value={loginTicket} />
              <input type="hidden" name="execution" value={executionKey} />
              <input type="hidden" name="_eventId" value="submit" />

              <div>
                  <input id="username" name="username" value={this.state.username} type="text"
                         ref="login"
                         className={this.loginError ? "invalid-input error" : "login-input"}
                         placeholder={translation("login.usernamePlaceholder")}
                         onChange={e => this.setState({username: e.target.value})}/>
                  <Overlay
                      show={this.loginError}
                      target={() => this.refs.login}
                      placement="right"
                      container={this.refs.login}
                    >
                      <Popover id="popover-contained">
                          {this.errorMsg}
                      </Popover>
                  </Overlay>
              </div>
              <div>
                  <input id="password" name="password"
                         type="password"
                         ref="password"
                         value=""
                         className={this.loginError ? "invalid-input error" : "login-input"}
                         placeholder={translation("login.passwordPlaceholder")}
                         onChange={e => this.setState({password: e.target.value})}/>
              </div>
              <div ref="pwreset" className="link" onClick={this.modeChange}>

                  {translation("login.forgotPassword")}

              </div>
              <button type="button" onClick={() => controller.doLogin(this.state.username, this.state.password) } className="btn btn-login">{translation("login.button")}</button>
             </form>
          </div>
      )
  }
}

