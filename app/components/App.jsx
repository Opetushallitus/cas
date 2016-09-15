import React from 'react';

import Login from './Login';
import Password from './Password'
import Header from './Header'
import Notices from './Notices'
import {translation} from '../resources/translations'
import CookieBanner from './CookieNotification'
import hakaImg from '../resources/img/haka_landscape_medium.gif'
import bgImage from '../resources/img/taustakuva.jpg'
import {ServiceList, ServiceDescriptions} from './Services'

const bgStyle = {
    backgroundImage:`url(${bgImage})`,
    backgroundSize: `cover`,
    backgroundRepeat: `no-repeat`,
};

const whiteStyle = {
    color: "white"
};
export default class App extends React.Component {
    constructor(props){
        super();
        document.title = translation("app.documentTitle");
    }

  render(){
    const state = this.props.state;
    const displayCookieBanner = !state.cookiesAccepted;
    const configuration = state.configuration;
    return(
      <div id="main" className="asdcontainer-fluid">
        {displayCookieBanner ? <CookieBanner controller={this.props.controller}/> : ""}
        <Header lang={state.lang} controller={this.props.controller}/>
        <Notices notices={state["notices"]}/>
        <div id="content" style={bgStyle}>
            <h1 style={whiteStyle} className="page-title">{translation("app.title")}</h1>
            <h2 style={whiteStyle} className="page-subtitle">{translation("app.subtitle")}</h2>
            <ServiceList/>

            <div className="box">
              {state.changingPassword ?
                <Password controller={this.props.controller} success={state.passwordResetSuccessful}/> :
                <Login controller={this.props.controller} error={state["error"]}
                       loginParams={state.bodyParams}
                       targetService={state.targetService}
                       submitForm={state.submitForm}/>}
            </div>
            <div>

            <div className="box">
              {translation("hakaLogin.description")}
              <a href={state.bodyParams.hakaUrl + "?redirect=" + state.targetService}>
                <img src={hakaImg} />
              </a>
           </div>
        </div>
      </div>
      <ServiceDescriptions/>
    </div>
    )
  }
}
