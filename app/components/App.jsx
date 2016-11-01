import React from 'react';

import Login from './Login';
import Password from './Password'
import Header from './Header'
import Notices from './Notices'
import {translation} from '../translations'
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

document.title = translation("app.documentTitle");

const App = ({controller, state}) => {

  const displayCookieBanner = !state.cookiesAccepted;
  return(
    <div id="main">
      {displayCookieBanner ? <CookieBanner onAccept={controller.acceptCookies}/> : ""}
      <Header lang={state.lang} changeLang={controller.changeLang}/>
      <Notices notices={state.notices}/>
      <div id="content" style={bgStyle}>
        <h1 style={whiteStyle} className="page-title">{translation("app.title")}</h1>
        <h2 style={whiteStyle} className="page-subtitle">{translation("app.subtitle")}</h2>
        <ServiceList/>

        <div className="box">
          {state.changingPassword ?
            <Password modeChange={controller.modeChange}
                      requestPassword={controller.requestPassword}
                      userName={state.passwordResetUsername}
                      resetStatus={state.passwordResetStatus}
                      onChange={controller.passwordResetUsernameChanged}/> :
            <Login modeChange={controller.modeChange}
                   loginError={state.loginError}
                   configuration={state.configuration}
                   targetService={state.targetService}
                   />}
        </div>
        <div>
          <div className="box">
            {translation("hakaLogin.description")}
            <a href={state.configuration.hakaUrl + "?redirect=" + state.targetService}>
              <img src={hakaImg} />
            </a>
         </div>
      </div>
    </div>
    <ServiceDescriptions/>
  </div>
  )
};

export default App;
