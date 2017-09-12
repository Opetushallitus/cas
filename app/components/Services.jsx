import React from 'react';
import services from '../resources/services.json';
import Scroll from 'react-scroll';
import {translation} from '../translations';

const scroller = Scroll.scroller;

function sortTranslation(a, b){
  if(translation(a+".name").toUpperCase() < translation(b+".name").toUpperCase()) return -1;
  if(translation(a+".name").toUpperCase() > translation(b+".name").toUpperCase()) return 1;
  return 0;
}

const ServiceList = () => {
  return(
    <div>
      <ul id="service-list">
        {Object.keys(services).sort(sortTranslation).map(k =>
        <li key={k}><a onClick={() =>
          scroller.scrollTo(k, {duration: 500, smooth: true })}>{translation(k+".shortname")+ " "}</a></li>)}
      </ul>
    </div>)
};


const ServiceDescriptions = ({configuration}) => {

    const loginTietosuojaselosteUrl = configuration.loginTietosuojaselosteUrl;

    return(
        <div className="container">
          <div className="services row">
            <h2 className="services-title">{translation("services.title")}</h2>
            {Object.keys(services).sort(sortTranslation).map(k =>
              <div className="service col-xs-12 col-sm-6 " key={k} id={k} name={k}>
                <span className={"bullet "+k}/>
                <div id="service-title">{translation((k+".name"))}</div>
                <div>{translation(k+".description")}</div>
              </div>)}
          </div>
            <div className="seloste">
                <a id="tietosuojaseloste" target="_blank" href={loginTietosuojaselosteUrl}>{translation("tietosuoja")}</a>
                {translation("rekisteri")}
                {Object.keys(services).filter(s => services[s].link).map(k =>
                    <span key={k} className="seloste-link">
                      {services[k].link ? <a target="_blank" href={services[k].link}>{translation(k+".shortname")}</a> : ""}
                    </span>
                )}
            </div>
        </div>
    )
};

module.exports = {ServiceList: ServiceList,
  ServiceDescriptions: ServiceDescriptions};
