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

export class ServiceList extends React.Component{
  render(){
    return(
      <div>
        <ul id="service-list">
          {Object.keys(services).sort(sortTranslation).map(k =>
            <li key={k}><a onClick={() =>
              scroller.scrollTo(k, {duration: 500, smooth: true })}>{translation(k+".shortname")+ " "}</a></li>)}
        </ul>
      </div>
    )
  }
}

export class ServiceDescriptions extends React.Component{
  render(){
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
                <a id="tietosuojaseloste" href="https://opintopolku.fi/wp/tietosuojaseloste/">{translation("tietosuoja")}</a>
                {translation("rekisteri")}
                {Object.keys(services).map(k =>
                    <span key={k} className="seloste-link">
                    {services[k].link ? <a href={services[k].link}>{translation((k+".shortname"))}</a> : ""}
                    </span>
                )}
            </div>
        </div>
    )
  }
}
