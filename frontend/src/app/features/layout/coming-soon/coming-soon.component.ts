import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-coming-soon',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './coming-soon.component.html',
  styleUrl: './coming-soon.component.scss'
})
export class ComingSoonComponent {
  @Input() title = 'Coming Soon';
  @Input() feature = 'This Feature';

  constructor(private route: ActivatedRoute) {
    this.route.data.subscribe(data => {
      if (data['title']) this.title = data['title'];
      if (data['feature']) this.feature = data['feature'];
    });
  }
}