import React from 'react';

export default class Notices extends React.Component {

  render(){
    const notices = this.props.notices.map(n =><div className="notice" key={n["id"]}> {n["text"]}</div>)
    return(
      <div className="notices">{notices}</div>
    )
  }
}